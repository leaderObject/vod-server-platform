package com.atfangyi.agent.runner;

import com.atfangyi.agent.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataIngestRunner implements CommandLineRunner {

    private final RagService ragService;

    @Override
    public void run(String... args) {
        log.info("启动数据同步到RAG知识库...");
        try {
            ragService.ingestFromDatabase();
            log.info("RAG知识库数据同步完成");
        } catch (Exception e) {
            log.warn("RAG知识库数据同步失败: {}", e.getMessage());
        }
    }
}
