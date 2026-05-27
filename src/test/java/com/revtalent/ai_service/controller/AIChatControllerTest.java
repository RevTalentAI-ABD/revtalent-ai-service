package com.revtalent.ai_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAskAIWithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ai/ask"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    public void testAskAIWithAuth_ShouldReturn405() throws Exception {
        // Since it's a POST, GET should return 405 Method Not Allowed or 400 Bad Request
        mockMvc.perform(get("/api/ai/ask"))
               .andExpect(status().isMethodNotAllowed());
    }
}
