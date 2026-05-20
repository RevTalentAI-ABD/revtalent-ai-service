package com.revtalent.ai_service.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OllamaEmbeddingService {

    public List<Double> createEmbedding(
            String text
    ) {

        RestTemplate restTemplate =
                new RestTemplate();

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "model",
                "nomic-embed-text"
        );

        body.put(
                "prompt",
                text
        );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "http://localhost:11434/api/embeddings",
                        entity,
                        Map.class
                );

        return (List<Double>)
                response.getBody()
                        .get("embedding");
    }
}
