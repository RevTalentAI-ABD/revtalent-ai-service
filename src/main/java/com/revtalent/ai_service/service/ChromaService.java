package com.revtalent.ai_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChromaService {

    private final String CHROMA_URL =
            "http://localhost:8000";

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

                    CHROMA_URL +

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

            List<Double> embedding

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

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(

                    CHROMA_URL +
                            "/api/v2/tenants/default_tenant/databases/default_database/collections/" +
                            collectionId +
                            "/add",

                    entity,

                    String.class
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // SEARCH SIMILAR CHUNKS

    public List<String> search(
            List<Double> embedding
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

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(

                            CHROMA_URL +
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
        }

        return List.of();
    }

    // GET COLLECTION ID

    private String getCollectionId()
            throws Exception {

        String response =
                restTemplate.getForObject(

                        CHROMA_URL +
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
