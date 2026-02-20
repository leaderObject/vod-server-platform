package com.atfangyi.tingshu.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.common.ParamAssert.ServiceAssert;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.service.KafkaService;
import com.atfangyi.tingshu.dto.SearchDto;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.album.BaseCategory3;
import com.atfangyi.tingshu.model.album.BaseCategoryView;
import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import com.atfangyi.tingshu.model.search.AttributeValueIndex;
import com.atfangyi.tingshu.model.search.SuggestIndex;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.query.search.AlbumIndexQuery;
import com.atfangyi.tingshu.search.receiver.SearchReceivver;
import com.atfangyi.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atfangyi.tingshu.search.repository.SuggestionIndexRepository;
import com.atfangyi.tingshu.search.service.SearchService;
import com.atfangyi.tingshu.user.client.AdminFeignClient;
import com.atfangyi.tingshu.user.client.UserFeignClient;
import com.atfangyi.tingshu.vo.album.AlbumStatVo;
import com.atfangyi.tingshu.vo.search.AlbumInfoIndexVo;
import com.atfangyi.tingshu.vo.search.AlbumSearchResponseVo;
import com.atfangyi.tingshu.vo.user.UserInfoVo;
import jodd.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.atfangyi.tingshu.common.constant.KafkaConstant.QUEUE_ALBUM_REMOVE;


@Slf4j
@Service
@SuppressWarnings({"all"})

public class SearchServiceImpl implements SearchService {

    private static final String ALBUM_INDEX = "albuminfo";
    private static final String SUGGEST_INDEX = "suggestinfo";

    @Autowired
    private AlbumInfoIndexRepository albumInfoIndexRepository;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SuggestionIndexRepository suggestionIndexRepository;

    @Autowired
    private SearchReceivver searchReceivver;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AdminFeignClient adminFeignClient;

    @Autowired
    private KafkaService kafkaService;


    @Override
    public boolean upperAlbum(Long albumId) {
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        //查询专辑信息根据AlbumId
        CompletableFuture<Long> longCompletableFuture = CompletableFuture.supplyAsync(() -> {
            AlbumInfo data = albumFeignClient.getAlbumInfoById(albumId).getData();
            Assert.notNull(data, "数据出现问题{}", data);
            BeanUtils.copyProperties(data, albumInfoIndex);
            BaseCategoryView baseCategoryView = albumFeignClient.getCategoryView(data.getCategory3Id()).getData();
            Assert.notNull(baseCategoryView, "数据出现问题{}", baseCategoryView);
            albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
            albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());
            albumInfoIndex.setCategory3Id(baseCategoryView.getCategory3Id());
            //5.1 随机为专辑产生播放量，订阅量，购买量，评论量 、
            int num1 = RandomUtil.randomInt(1000, 2000);
            int num2 = RandomUtil.randomInt(500, 1000);
            int num3 = RandomUtil.randomInt(200, 400);
            int num4 = RandomUtil.randomInt(100, 200);
            albumInfoIndex.setPlayStatNum(num1);
            albumInfoIndex.setSubscribeStatNum(num2);
            albumInfoIndex.setBuyStatNum(num3);
            albumInfoIndex.setCommentStatNum(num4);
            //5.2 基于统计值计算出专辑得分 为不同统计类型设置不同权重
            BigDecimal bigDecimal1 = new BigDecimal(num4).multiply(new BigDecimal("0.4"));
            BigDecimal bigDecimal2 = new BigDecimal(num3).multiply(new BigDecimal("0.3"));
            BigDecimal bigDecimal3 = new BigDecimal(num2).multiply(new BigDecimal("0.2"));
            BigDecimal bigDecimal4 = new BigDecimal(num1).multiply(new BigDecimal("0.1"));
            BigDecimal hotScore = bigDecimal1.add(bigDecimal2).add(bigDecimal3).add(bigDecimal4);
            albumInfoIndex.setHotScore(hotScore.doubleValue());
            return data.getUserId();
        });
        //查询用户Id 根据AlbumInfo信息去查询
        CompletableFuture<Void> thenAcceptAsync = longCompletableFuture.thenAcceptAsync(userId -> {
            UserInfo userInfo = userFeignClient.queryUserInfoByUserId(userId);
            Assert.notNull(userInfo, "数据出现问题{}", userInfo);
            albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        });

        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
            //查询属性集合信息
            albumInfoIndex.setAttributeValueIndexList(albumFeignClient.findAlbumAttributeValue(albumId).getData().stream().map(albumAttributeValue -> {
                AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                BeanUtils.copyProperties(albumAttributeValue, attributeValueIndex);
                return attributeValueIndex;
            }).collect(Collectors.toList()));
        });
        CompletableFuture.allOf(thenAcceptAsync, longCompletableFuture, runAsync).join();
        //上传专辑到es
        albumInfoIndexRepository.save(albumInfoIndex);
        log.info("数据保存成功--albumInfoIndex{}", albumInfoIndex);
        SuggestIndex suggestIndex = new SuggestIndex();
        suggestIndex.setId(albumId.toString());
        suggestIndex.setTitle(albumInfoIndex.getAlbumTitle());
        suggestIndex.setKeyword(new Completion(new String[]{albumInfoIndex.getAlbumTitle()}));
        suggestIndex.setKeywordPinyin(new Completion(new String[]{PinyinUtil.getPinyin(albumInfoIndex.getAlbumTitle(), "")}));
        suggestIndex.setKeywordSequence(new Completion(new String[]{PinyinUtil.getFirstLetter(albumInfoIndex.getAlbumTitle(), "")}));
        suggestionIndexRepository.save(suggestIndex);
        log.info("数据保存成功--suggestIndex{}", suggestIndex);
        return true;
    }

    /**
     * 下架
     *
     * @param value
     */
    @Override
    public void AlbumLower(String value) {
        albumInfoIndexRepository.deleteById(Long.valueOf(value));
    }

    @Override
    public void AlbumUpper(String value) {
        upperAlbum(Long.valueOf(value));
    }

    @Override
    @SneakyThrows
    public AlbumSearchResponseVo albumInfo(AlbumIndexQuery albumIndexQuery) {

        SearchResponse<AlbumInfoIndex> search = elasticsearchClient.search(this.BuildDSL(albumIndexQuery), AlbumInfoIndex.class);
        log.info("查询结果DSL{}", search.toString());
        AlbumSearchResponseVo albumSearchResponseVo = this.pardeResult(search);
        albumSearchResponseVo.setPageNo(albumIndexQuery.getPageNo());
        albumSearchResponseVo.setPageSize(albumIndexQuery.getPageSize());
        long TotalPage = albumSearchResponseVo.getTotal() / albumSearchResponseVo.getPageSize();
        TotalPage = (albumSearchResponseVo.getTotal() % albumSearchResponseVo.getPageSize()) != 0 ? TotalPage + 1 : TotalPage;
        albumSearchResponseVo.setTotalPages(TotalPage);
        return albumSearchResponseVo;
    }

    private AlbumSearchResponseVo pardeResult(SearchResponse<AlbumInfoIndex> search) {
        AlbumSearchResponseVo albumSearchResponseVo = new AlbumSearchResponseVo();
        HitsMetadata<AlbumInfoIndex> hits = search.hits();
        albumSearchResponseVo.setTotal(hits.total().value());
        albumSearchResponseVo.setList(hits.hits().stream().map(albumInfoIndexVoHit -> {
            AlbumInfoIndex source = albumInfoIndexVoHit.source();
            if (albumInfoIndexVoHit.highlight() != null && albumInfoIndexVoHit.highlight().containsKey("albumTitle")) {
                source.setAlbumTitle(albumInfoIndexVoHit.highlight().get("albumTitle").get(0));
            }
            AlbumInfoIndexVo infoIndexVo = new AlbumInfoIndexVo();
            BeanUtils.copyProperties(source, infoIndexVo);
            return infoIndexVo;
        }).collect(Collectors.toList()));
        return albumSearchResponseVo;
    }

    private SearchRequest BuildDSL(AlbumIndexQuery albumIndexQuery) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(ALBUM_INDEX);
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        //关键字不为空
        if (!StringUtils.isEmpty(albumIndexQuery.getKeyword())) {
            BoolQuery.Builder keywordBoolQuery = new BoolQuery.Builder();
            keywordBoolQuery.should(s -> s.match(m -> m.field("albumTitle").query(albumIndexQuery.getKeyword())));
            keywordBoolQuery.should(s -> s.match(m -> m.field("albumIntro").query(albumIndexQuery.getKeyword())));
            keywordBoolQuery.should(s -> s.term(t -> t.field("announcerName").value(albumIndexQuery.getKeyword())));
            boolQuery.must(keywordBoolQuery.build()._toQuery());
        }

        List<String> attributeList = albumIndexQuery.getAttributeList();
        if (!CollectionUtils.isEmpty(attributeList)) {
            attributeList.stream().forEach(attribute -> {
                String[] split = attribute.split(":");
                String attributeId = split[0];
                String valueId = split[1];
                BoolQuery.Builder NestedBool = new BoolQuery.Builder();
                NestedBool.must(m -> m.term(t -> t.field("attributeId").value(attributeId)));
                NestedBool.must(m -> m.term(t -> t.field("valueId").value(valueId)));
                boolQuery.must(m -> m.nested(n -> n.path("attributeValueIndexList").query(NestedBool.build()._toQuery())));
            });
        }
        if (albumIndexQuery.getCategory1Id() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category1Id").value(albumIndexQuery.getCategory1Id())));
        }
        if (albumIndexQuery.getCategory2Id() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category2Id").value(albumIndexQuery.getCategory2Id())));
        }
        if (albumIndexQuery.getCategory3Id() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category3Id").value(albumIndexQuery.getCategory3Id())));
        }
        builder.query(boolQuery.build()._toQuery());
        builder.from((albumIndexQuery.getPageNo() - 1) * albumIndexQuery.getPageSize());
        builder.size(albumIndexQuery.getPageSize());
        builder.highlight(h -> h.fields("albumTitle", f -> f.preTags("<font color='red'>").postTags("</font>")));
        if (!StringUtils.isEmpty(albumIndexQuery.getOrder())) {
            //@Schema(description = "排序（综合排序[1:desc] 播放量[2:desc] 发布时间[3:desc]；asc:升序 desc:降序）")
            String[] split = albumIndexQuery.getOrder().split(":");
            SortOrder sortOrder = "desc".equals(split[1]) ? SortOrder.Desc : SortOrder.Asc;
            switch (split[0]) {
                case "1":
                    builder.sort(s -> s.field(f -> f.field("_score").order(sortOrder)));
                    break;
                case "2":
                    builder.sort(s -> s.field(f -> f.field("playStatNum").order(sortOrder)));
                    break;
                case "3":
                    builder.sort(s -> s.field(f -> f.field("createTime").order(sortOrder)));
            }
        }
        builder.source(s -> s.filter(f -> f.excludes(Arrays.asList("category1Id", "category2Id", "category3Id"))));
        return builder.build();
    }


    @Override
    @SneakyThrows
    public List<Map<String, Object>> channel(Long category1Id) {
        List<BaseCategory3> baseCategory3s = albumFeignClient.findTopBaseCategory3(category1Id).getData();
        List<Long> collect = baseCategory3s.stream().map(baseCategory3 -> baseCategory3.getId()).collect(Collectors.toList());
        Map<Long, BaseCategory3> baseCategory3Map = baseCategory3s.stream().collect(Collectors.toMap(baseCategory3 -> baseCategory3.getId(), baseCategory3 -> baseCategory3));
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(ALBUM_INDEX);
        if (!CollectionUtils.isEmpty(collect)) {
            builder.query(q -> q.terms(t -> t.field("category3Id").terms(te -> te.value(collect.stream().map(l -> {
                return FieldValue.of(l);
            }).collect(Collectors.toList())))));
        }
        builder.aggregations("category3IdAggs", a -> a.terms(t -> t.field("category3Id").size(50)).aggregations("TopAggs", ag -> ag.topHits(t -> t.size(10).sort(s -> s.field(f -> f.field("hotScore").order(SortOrder.Desc))))));
        SearchResponse<AlbumInfoIndex> search = elasticsearchClient.search(builder.build(), AlbumInfoIndex.class);
        List<LongTermsBucket> category3IdAggs = search.aggregations().get("category3IdAggs").lterms().buckets().array();
        if (!CollectionUtil.isEmpty(category3IdAggs)) {
            return category3IdAggs.stream().map(albumInfoIndex -> {
                HashMap<String, Object> baseCategory3ListHashMap = new HashMap<>();
                long key = albumInfoIndex.key();
                BaseCategory3 baseCategory3 = baseCategory3Map.get(key);
                List<Hit<JsonData>> topAggs = albumInfoIndex.aggregations().get("TopAggs").topHits().hits().hits();
                baseCategory3ListHashMap.put("baseCategory3", baseCategory3);
                baseCategory3ListHashMap.put("list", topAggs.stream().map(jsonDataHit -> {
                    JsonData source = jsonDataHit.source();
                    AlbumInfoIndex parsedObject = JSONObject.parseObject(source.toString(), AlbumInfoIndex.class);
                    log.info("数据source{}", source);
                    log.info("数据infoIndex{}", parsedObject);
                    return parsedObject;
                }).collect(Collectors.toList()));
                return baseCategory3ListHashMap;
            }).collect(Collectors.toList());
        }
        return null;

    }

    @Override
    @SneakyThrows
    public List<String> completeSuggest(String keyword) {
        log.error("elk 测试日志代码");
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(SUGGEST_INDEX);
        builder.suggest(s -> s.suggesters("mySuggestKeyword", su -> su.prefix(keyword).completion(c -> c.field("keyword").size(15).skipDuplicates(true))));
        builder.suggest(s -> s.suggesters("mySuggestPinyin", su -> su.prefix(keyword).completion(c -> c.field("keywordPinyin").size(15).skipDuplicates(true))));
        builder.suggest(s -> s.suggesters("mySuggestLetter", su -> su.prefix(keyword).completion(c -> c.field("keywordSequence").size(15).skipDuplicates(true))));
        SearchResponse<SuggestIndex> suggestIndexSearchResponse = elasticsearchClient.search(builder.build(), SuggestIndex.class);
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        hashSet.addAll(this.ParseRedust("mySuggestKeyword", suggestIndexSearchResponse));
        hashSet.addAll(this.ParseRedust("mySuggestPinyin", suggestIndexSearchResponse));
        hashSet.addAll(this.ParseRedust("mySuggestLetter", suggestIndexSearchResponse));
        //要求 数据量必须至少为15条
        if (hashSet.size() < 15) {
            SearchRequest.Builder searchBuild = new SearchRequest.Builder();
            searchBuild.index(ALBUM_INDEX);
            searchBuild.query(q -> q.match(m -> m.field("albumTitle").query(keyword)));
            SearchResponse<AlbumInfoIndex> search = elasticsearchClient.search(searchBuild.build(), AlbumInfoIndex.class);
            List<Hit<AlbumInfoIndex>> hits = search.hits().hits();
            if (!CollectionUtils.isEmpty(hits)) {
                if (hits.size() > hashSet.size()) {
                    return hits.stream().map(albumInfoIndexHit -> {
                        return albumInfoIndexHit.source().getAlbumTitle();
                    }).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>(hashSet);
    }

    @Override
    public Map<String, Object> queryAlbumInfo(Long albumId) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        if (!bloomFilter.contains(albumId)) {
            throw new GuiguException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        HashMap<String, Object> map = new HashMap<>();
        CompletableFuture<AlbumInfo> AlbumCompletableFuture = CompletableFuture.supplyAsync(() -> {
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(albumId).getData();
            if (albumInfo == null) {
                log.error("数据异常 method queryAlbumInfo->albumInfo", albumInfo);
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
            map.put("albumInfo", albumInfo);
            return albumInfo;
        });

        CompletableFuture<UserInfo> userInfoCompletableFuture = AlbumCompletableFuture.thenApplyAsync(t -> {
            UserInfo userInfo = userFeignClient.queryUserInfoByUserId(t.getUserId());
            if (userInfo == null) {
                log.error("数据异常 method queryAlbumInfo->userInfo", userInfo);
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(userInfo, userInfoVo);
            map.put("announcer", userInfoVo);
            return userInfo;
        });
        CompletableFuture<Void> voidCompletableFuture = AlbumCompletableFuture.thenAcceptAsync(albumInfo -> {
            BaseCategoryView baseCategoryView = albumFeignClient.getCategoryView(albumInfo.getCategory3Id()).getData();
            map.put("baseCategoryView", baseCategoryView);
        });

        CompletableFuture<Void> thennedAcceptAsync = AlbumCompletableFuture.thenAcceptAsync(albumInfo -> {
            AlbumStatVo albumStatVo = albumFeignClient.getAlbumStatVo(albumInfo.getId());
            map.put("albumStatVo", albumStatVo);
        });

        CompletableFuture.allOf(AlbumCompletableFuture, userInfoCompletableFuture, voidCompletableFuture, thennedAcceptAsync).join();
        return map;
    }

    @Override
    public void updateLatelyAlbumRanking() {
        List<Long> collect = albumFeignClient.queryAllCategoryInfo().stream().map(baseCategory1 -> baseCategory1.getId()).collect(Collectors.toList());
        String[] types = new String[]{"hotScore", "playStatNum", "subscribeStatNum", "buyStatNum", "commentStatNum"};
        collect.stream().forEach(l -> {
            for (String type : types) {
                redisTemplate.opsForHash().put(RedisConstant.RANKING_KEY_PREFIX + String.valueOf(l), type, this.PaeseResult(l, type));
            }
        });
        log.info("热度更新完成");
    }

    @Override
    public List<AlbumInfoIndex> findRankingList(Long category1Id, String dimension) {
        return  (List<AlbumInfoIndex>) redisTemplate.opsForHash().get(RedisConstant.RANKING_KEY_PREFIX + String.valueOf(category1Id), dimension);
    }

    @Override
    public Map<String, Object> queryAlbumDetailCount() {
        HashMap<String, Object> map = new HashMap<>();
        AtomicReference<Integer> AlbumCount = new AtomicReference<>(0);
        albumInfoIndexRepository.findAll().forEach(albumInfoIndex -> AlbumCount.getAndSet(AlbumCount.get() + 1));
        map.put("albumTotal", AlbumCount.get());
        /**
         * 使用异步编排提升查询各个微服务效率
         */
        AtomicReference<Integer> showCount = new AtomicReference<>(0);
        albumInfoIndexRepository.findAll().forEach(albumInfoIndex -> showCount.updateAndGet(v -> v + albumInfoIndex.getPlayStatNum()));
        map.put("showCount", showCount.get());
        CompletableFuture.allOf(CompletableFuture.runAsync(() -> {
            map.put("TrackTotal", albumFeignClient.findAlbumTrackCount());
        }), CompletableFuture.runAsync(() -> {
            map.put("userTotal", adminFeignClient.getUserInfoCount());
        })).join();

        return map;
    }

    @Override
    public Map<String, Object> queryAlbumPageByCondition(SearchDto searchDto, Integer page, Integer size) {
        HashMap<String, Object> map = new HashMap<>();
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(ALBUM_INDEX);
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        if (searchDto != null) {
            if (StringUtil.isNotBlank(searchDto.getAlbumTitle()))
                boolBuilder.must(m -> m.match(ma -> ma.field("albumTitle").query(searchDto.getAlbumTitle())));
            if (searchDto.getCategory1Id() != null)
                boolBuilder.filter(f -> f.term(t -> t.field("category1Id").value(searchDto.getCategory1Id())));
            if (searchDto.getCategory2Id() != null)
                boolBuilder.filter(f -> f.term(t -> t.field("category2Id").value(searchDto.getCategory2Id())));
            if (searchDto.getCategory3Id() != null)
                boolBuilder.filter(f -> f.term(t -> t.field("category3Id").value(searchDto.getCategory3Id())));
        }
        try {
            builder.query(q -> q.bool(boolBuilder.build()));
            builder.highlight(h -> h.fields("albumTitle", f -> f.preTags("<font color='red'>").postTags("</font>")));
            builder.from((page - 1) * size).size(size);
            SearchResponse<AlbumInfoIndex> albumInfoIndexSearchResponse = elasticsearchClient.search(builder.build(), AlbumInfoIndex.class);
            List<AlbumInfoIndex> infoIndexList = albumInfoIndexSearchResponse.hits().hits().stream().map(albumInfoIndexHit -> {
                AlbumInfoIndex albumInfoIndex = albumInfoIndexHit.source();
                List<String> albumTitle = albumInfoIndexHit.highlight().get("albumTitle");
                if (!CollectionUtils.isEmpty(albumTitle)) albumInfoIndex.setAlbumTitle(albumTitle.get(0));
                return albumInfoIndex;
            }).collect(Collectors.toList());
            map.put("albumInfoList", infoIndexList);
            //获取数据量
            long count = albumInfoIndexSearchResponse.hits().total().value();
            long PageSize = count % size == 0 ? count / size : count / size + 1;
            map.put("total", count);
            map.put("totalPage", PageSize);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean removeAlbumById(Long id) {
        albumInfoIndexRepository.deleteById(id);
        // I don't care
        kafkaService.sendMessage(QUEUE_ALBUM_REMOVE, id.toString());
        return true;
    }

    @Override
    public boolean updateAlbumInfoById(AlbumDto albumDto) {
        boolean flag = false;
        ServiceAssert.ParamAssert(albumDto);
        AlbumInfoIndex albumInfoIndex = albumInfoIndexRepository.findById(albumDto.getId()).get();
        ServiceAssert.ObjectAssert(albumInfoIndex);
        if (albumFeignClient.updateAlbumInfoById(albumDto)) {
            BeanUtils.copyProperties(albumDto, albumInfoIndex);
            albumInfoIndexRepository.save(albumInfoIndex);
            flag = true;
        }
        return flag;
    }

    @SneakyThrows
    private List<AlbumInfoIndex> PaeseResult(Long l, String type) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(ALBUM_INDEX);
        builder.query(q -> q.term(t -> t.field("category1Id").value(l)));
        builder.sort(s -> s.field(f -> f.field(type).order(SortOrder.Desc)));
        builder.size(20);
        List<Hit<AlbumInfoIndex>> hits = elasticsearchClient.search(builder.build(), AlbumInfoIndex.class).hits().hits();
        if (hits != null) {
            return hits.stream().map(hit -> hit.source()).collect(Collectors.toList());
        }
        return null;

    }

    private List<String> ParseRedust(String keyword, SearchResponse<SuggestIndex> suggestIndexSearchResponse) {
        List<String> strings = new ArrayList<>();
        List<Suggestion<SuggestIndex>> suggestions = suggestIndexSearchResponse.suggest().get(keyword);
        if (!CollectionUtils.isEmpty(suggestions)) {
            for (Suggestion<SuggestIndex> suggestion : suggestions) {
                List<CompletionSuggestOption<SuggestIndex>> options = suggestion.completion().options();
                if (!CollectionUtils.isEmpty(options)) {
                    strings = options.stream().map(option -> {
                        return option.source().getTitle();
                    }).collect(Collectors.toList());
                }
            }
        }
        return strings;
    }
}
