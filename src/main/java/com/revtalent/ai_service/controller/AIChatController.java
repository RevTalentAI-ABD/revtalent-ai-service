package com.revtalent.ai_service.controller;

import com.revtalent.ai_service.dto.AIRequest;
import com.revtalent.ai_service.model.AIDocument;
import com.revtalent.ai_service.repository.AIDocumentRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import com.revtalent.ai_service.service.ChromaService;
import com.revtalent.ai_service.service.OllamaEmbeddingService;
import com.revtalent.ai_service.util.SecurityUserContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class AIChatController {

    @Autowired
    private ChromaService chromaService;

    @Autowired
    private OllamaEmbeddingService embeddingService;

    @Autowired
    private SecurityUserContext securityUserContext;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAI(
            @RequestBody AIRequest request
    ) {
        try {
            String question = request.getQuestion();
            List<Double> embedding = embeddingService.createEmbedding(question);

            Long userId = securityUserContext.getCurrentUserId();

            List<String> chunks = chromaService.search(embedding, userId);

            StringBuilder context =
                    new StringBuilder();

            for (String chunk : chunks) {

                context.append(chunk)
                        .append("\n\n");
            }

            // FINAL PROMPT

            String finalPrompt =

                    """
                    You are an intelligent HR AI assistant for the company RevTalent.

                    Answer the user's question directly and concisely using ONLY the provided HR Documents Context.
                    DO NOT mention that you are getting this from a document, and DO NOT say phrases like "as per the provided documents" or "in the documents". Just give the exact, pin-point answer.

                    If the exact answer is not found in the context, provide a general related answer based on your knowledge, but keep it strictly professional and to the point. DO NOT mention that you couldn't find it in the documents.

                    HR Documents Context:
                    """
                            +

                            context +

                            """

                            User Question:
                            """
                            +

                            question;

            // OLLAMA REQUEST

            RestTemplate restTemplate =
                    new RestTemplate();

            Map<String, Object> body =
                    new HashMap<>();

            body.put(
                    "model",
                    "mistral"
            );

            body.put(
                    "prompt",
                    finalPrompt
            );

            body.put(
                    "stream",
                    false
            );

            body.put(
                    "keep_alive",
                    "30m"
            );

            body.put(

                    "options",

                    Map.of(

                            "num_predict",
                            200,

                            "temperature",
                            0.2,

                            "top_k",
                            20,

                            "top_p",
                            0.8
                    )
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

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(

                            "http://localhost:11434/api/generate",

                            entity,

                            Map.class
                    );

            String aiResponse =
                    response.getBody()
                            .get("response")
                            .toString();

            return ResponseEntity.ok(Map.of(
                    "response",
                    aiResponse
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("response", "AI failed: " + e.getMessage()));
        }
    }

    @PostMapping("/screen-resume")
    public ResponseEntity<Map<String, String>> screenResume(@RequestBody Map<String, String> request) {
        try {
            String resumeText = request.get("resumeText");
            String jobDescription = request.get("jobDescription");

            String prompt = "You are an expert HR AI assistant.\n\n" +
                    "Review the following resume against the job description.\n" +
                    "Provide a brief summary of their qualifications, strengths, and weaknesses.\n" +
                    "At the very end of your response, on a new line, you MUST output a score out of 100 in this exact format:\n" +
                    "SCORE: 85\n\n" +
                    "Job Description:\n" + jobDescription + "\n\n" +
                    "Resume:\n" + resumeText;

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> body = new HashMap<>();
            body.put("model", "mistral");
            body.put("prompt", prompt);
            body.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:11434/api/generate", entity, Map.class);
            String aiResponse = response.getBody().get("response").toString();

            return ResponseEntity.ok(Map.of("response", aiResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("response", "AI failed: " + e.getMessage()));
        }
    }
}
