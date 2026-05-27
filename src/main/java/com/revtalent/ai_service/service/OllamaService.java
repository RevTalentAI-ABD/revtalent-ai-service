package com.revtalent.ai_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private String generateUrl() {
        return ollamaBaseUrl.replaceAll("/$", "") + "/api/generate";
    }

    private String callOllama(String prompt) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "mistral");
            body.put("prompt", prompt);
            body.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(generateUrl(), request, Map.class);

            if (response.getBody() != null && response.getBody().get("response") != null) {
                return response.getBody().get("response").toString();
            }

            return "⚠ No response from AI";

        } catch (Exception e) {
            return "❌ AI Error: " + e.getMessage();
        }
    }

    public String askHR(String question) {
        String prompt = """
                You are a professional HR assistant.
                Answer clearly, professionally, and concisely.

                Question:
                """ + question;
        return callOllama(prompt);
    }

    public String screenResume(String resume, String job) {
        String prompt = """
                You are an HR recruiter.
                Compare the resume with the job description.
                IMPORTANT: Your response MUST start exactly with "SCORE: X" where X is a number from 0 to 100 representing the match percentage.
                Then, on a new line, provide:
                - Highlight strengths
                - Highlight gaps
                - Final recommendation

                Resume:
                """ + resume + """

                Job Description:
                """ + job;
        return callOllama(prompt);
    }

    public String performanceSummary(String history) {
        String prompt = """
                You are an HR manager.
                Analyze employee performance data and:
                - Summarize performance
                - Identify strengths
                - Identify weaknesses
                - Suggest improvements

                Data:
                """ + history;
        return callOllama(prompt);
    }

    public String generatePolicy(String topic) {
        String prompt = """
                Generate a professional HR policy for:
                """ + topic + """

                Include:
                - Purpose
                - Scope
                - Rules
                - Compliance
                """;
        return callOllama(prompt);
    }
}
