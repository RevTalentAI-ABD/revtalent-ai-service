package com.revtalent.ai_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChromaService {

    @Value("${chroma.base-url:http://localhost:8000}")
    private String chromaBaseUrl;

    private String chromaUrl() {
        return chromaBaseUrl.replaceAll("/$", "");
    }

    private final String COLLECTION =
            "hr_documents";

    private final RestTemplate restTemplate =
            new RestTemplate();

    private final ObjectMapper mapper =
            new ObjectMapper();

    // CREATE COLLECTION

    public void createCollection() {

        try {

            Map<String, Object> body =
                    new HashMap<>();

            body.put(
                    "name",
                    COLLECTION
            );

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(
                            body,
                            headers
                    );

            restTemplate.postForEntity(

                    chromaUrl() +

                            "/api/v2/tenants/default_tenant/databases/default_database/collections",

                    entity,

                    String.class
            );

            System.out.println(
                    "Chroma collection created"
            );

        } catch (Exception e) {

            System.out.println(
                    e.getMessage()
            );
        }
    }
    // STORE CHUNK

    public void storeChunk(
            String id,
            String chunk,
            List<Double> embedding,
            Long userId
    ) {
        try {

            String collectionId =
                    getCollectionId();

            Map<String, Object> body =
                    new HashMap<>();

            body.put(
                    "ids",
                    List.of(id)
            );

            body.put(
                    "documents",
                    List.of(chunk)
            );

            body.put(
                    "embeddings",
                    List.of(embedding)
            );

            if (userId != null) {
                body.put("metadatas", List.of(Map.of("userId", userId)));
            }

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    chromaUrl() +
                            "/api/v2/tenants/default_tenant/databases/default_database/collections/" +
                            collectionId +
                            "/add",
                    entity,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Chroma indexing failed: " + response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store chunk in Chroma: " + e.getMessage(), e);
        }
    }

    // SEARCH SIMILAR CHUNKS

    public List<String> search(
            List<Double> embedding,
            Long userId
    ) {
        try {

            String collectionId =
                    getCollectionId();

            Map<String, Object> body =
                    new HashMap<>();

            body.put(
                    "query_embeddings",
                    List.of(embedding)
            );

            body.put(
                    "n_results",
                    4
            );

            if (userId != null) {
                body.put("where", Map.of("userId", userId));
            }

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(

                            chromaUrl() +
                                    "/api/v2/tenants/default_tenant/databases/default_database/collections/" +
                                    collectionId +
                                    "/query",

                            entity,

                            String.class
                    );

            JsonNode json =
                    mapper.readTree(
                            response.getBody()
                    );

            List<String> chunks =
                    new ArrayList<>();

            JsonNode docs =
                    json.get("documents")
                            .get(0);

            for (JsonNode doc : docs) {

                chunks.add(
                        doc.asText()
                );
            }

            return chunks;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to search Chroma: " + e.getMessage(), e);
        }
    }

    // GET COLLECTION ID

    private String getCollectionId()
            throws Exception {

        String response =
                restTemplate.getForObject(

                        chromaUrl() +
                                "/api/v2/tenants/default_tenant/databases/default_database/collections",

                        String.class
                );

        JsonNode json =
                mapper.readTree(response);

        for (JsonNode node : json) {

            if (node.get("name")
                    .asText()
                    .equals(COLLECTION)) {

                return node.get("id")
                        .asText();
            }
        }

        throw new RuntimeException(
                "Collection not found"
        );
    }
}
