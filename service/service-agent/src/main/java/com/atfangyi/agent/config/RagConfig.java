package com.atfangyi.agent.config;

import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class RagConfig {

    private static final String VECTOR_STORE_FILE = "vector-store.json";

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = new File(VECTOR_STORE_FILE);
        if (vectorStoreFile.exists()) {
            vectorStore.load(vectorStoreFile);
        }
        return vectorStore;
    }
}
