package com.atfangyi.tingshu.search.service;

import com.atfangyi.tingshu.dto.SearchDto;
import com.atfangyi.tingshu.dto.album.AlbumDto;
import com.atfangyi.tingshu.model.search.AlbumInfoIndex;
import com.atfangyi.tingshu.query.search.AlbumIndexQuery;
import com.atfangyi.tingshu.vo.search.AlbumSearchResponseVo;

import java.util.List;
import java.util.Map;

public interface SearchService {


    boolean upperAlbum(Long albumId);


    void AlbumLower(String value);

    void AlbumUpper(String value);

    AlbumSearchResponseVo albumInfo(AlbumIndexQuery albumIndexQuery);

    List<Map<String , Object>> channel(Long category1Id);

    List<String> completeSuggest(String keyword);

    Map<String, Object> queryAlbumInfo(Long albumId);

    void updateLatelyAlbumRanking();

    List<AlbumInfoIndex> findRankingList(Long category1Id, String dimension);

    Map<String, Object> queryAlbumDetailCount();

    Map<String,Object> queryAlbumPageByCondition(SearchDto searchDto, Integer page, Integer size);

    boolean removeAlbumById(Long id);


    boolean updateAlbumInfoById(AlbumDto albumDto);

}
