package com.revtalent.ai_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    public void testUploadDocumentPathTraversal_ShouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/upload").file(file))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    public void testUploadDocumentNullFilename_ShouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/upload").file(file))
               .andExpect(status().isBadRequest());
    }
}
