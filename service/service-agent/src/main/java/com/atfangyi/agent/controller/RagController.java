package com.atfangyi.agent.controller;

import com.atfangyi.agent.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@CrossOrigin
public class RagController {

    private final RagService ragService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            ragService.ingestFromFile(file.getResource());
            result.put("success", true);
            result.put("message", "文档导入成功: " + file.getOriginalFilename());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文档导入失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sync-db")
    public ResponseEntity<Map<String, Object>> syncFromDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            ragService.ingestFromDatabase();
            result.put("success", true);
            result.put("message", "数据库同步成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据库同步失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> search(@RequestParam String query) {
        List<Document> results = ragService.retrieve(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        int count = ragService.getDocumentCount();
        status.put("hasData", count != 0);
        status.put("vectorStoreType", "SimpleVectorStore");
        return ResponseEntity.ok(status);
    }
}
