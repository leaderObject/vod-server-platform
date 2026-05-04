package com.atfangyi.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    private static final String VECTOR_STORE_FILE = "vector-store.json";

    public void ingestDocuments(List<Document> documents) {
        vectorStore.add(documents);
        saveToFile();
        log.info("导入 {} 个文档到知识库", documents.size());
    }

    public List<Document> retrieve(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(5).build()
        );
    }

    public void ingestFromFile(Resource resource) {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        ingestDocuments(documents);
    }

    public void ingestFromDatabase() {
        List<Document> documents = new ArrayList<>();

        // 专辑信息
        List<Document> albumDocs = queryAlbumInfo();
        documents.addAll(albumDocs);

        // 分类信息
        List<Document> categoryDocs = queryCategoryInfo();
        documents.addAll(categoryDocs);

        // VIP配置
        List<Document> vipDocs = queryVipConfig();
        documents.addAll(vipDocs);

        if (!documents.isEmpty()) {
            ingestDocuments(documents);
        }
    }

    private List<Document> queryAlbumInfo() {
        String sql = "SELECT id, album_title, album_intro, album_tags FROM album_info WHERE is_deleted = 0 LIMIT 200";
        List<Document> docs = new ArrayList<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                String id = rs.getString("id");
                String title = rs.getString("album_title");
                String intro = rs.getString("album_intro");
                String tags = rs.getString("album_tags");

                String content = "专辑名称：" + title;
                if (intro != null && !intro.isEmpty()) {
                    content += "。简介：" + intro;
                }
                if (tags != null && !tags.isEmpty()) {
                    content += "。标签：" + tags;
                }

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("source", "album_info");
                metadata.put("album_id", id);
                metadata.put("title", title);

                docs.add(new Document(content, metadata));
            });
            log.info("从数据库导入 {} 条专辑信息", docs.size());
        } catch (Exception e) {
            log.warn("导入专辑信息失败: {}", e.getMessage());
        }
        return docs;
    }

    private List<Document> queryCategoryInfo() {
        String sql = "SELECT id, name FROM base_category1 WHERE is_deleted = 0";
        List<Document> docs = new ArrayList<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                String id = rs.getString("id");
                String name = rs.getString("name");

                String content = "一级分类：" + name;
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("source", "base_category1");
                metadata.put("category_id", id);

                docs.add(new Document(content, metadata));
            });
            log.info("从数据库导入 {} 条分类信息", docs.size());
        } catch (Exception e) {
            log.warn("导入分类信息失败: {}", e.getMessage());
        }
        return docs;
    }

    private List<Document> queryVipConfig() {
        String sql = "SELECT id, service_name, service_desc, price, original_price FROM vip_service_config WHERE is_deleted = 0";
        List<Document> docs = new ArrayList<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                String id = rs.getString("id");
                String name = rs.getString("service_name");
                String desc = rs.getString("service_desc");
                String price = rs.getString("price");
                String originalPrice = rs.getString("original_price");

                String content = "VIP服务：" + name;
                if (desc != null && !desc.isEmpty()) {
                    content += "。描述：" + desc;
                }
                content += "。价格：" + price + "元";
                if (originalPrice != null) {
                    content += "，原价：" + originalPrice + "元";
                }

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("source", "vip_service_config");
                metadata.put("config_id", id);

                docs.add(new Document(content, metadata));
            });
            log.info("从数据库导入 {} 条VIP配置", docs.size());
        } catch (Exception e) {
            log.warn("导入VIP配置失败: {}", e.getMessage());
        }
        return docs;
    }

    private void saveToFile() {
        if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
            simpleVectorStore.save(new File(VECTOR_STORE_FILE));
            log.info("向量库已持久化到 {}", VECTOR_STORE_FILE);
        }
    }

    public int getDocumentCount() {
        // SimpleVectorStore 没有直接计数方法，通过检索测试
        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder().query("测试").topK(1).build()
            );
            return results.isEmpty() ? 0 : -1; // -1表示有数据但不确定数量
        } catch (Exception e) {
            return 0;
        }
    }
}
