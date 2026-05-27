package com.revtalent.ai_service.controller;

import com.revtalent.ai_service.model.AIDocument;

import com.revtalent.ai_service.repository.AIDocumentRepository;

import com.revtalent.ai_service.service.ChromaService;
import com.revtalent.ai_service.service.OllamaEmbeddingService;
import com.revtalent.ai_service.service.TextChunkService;
import com.revtalent.ai_service.util.SecurityUserContext;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.file.Files;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class AIDocumentController {

    @Autowired
    private AIDocumentRepository repository;

    @Autowired
    private TextChunkService chunkService;

    @Autowired
    private OllamaEmbeddingService embeddingService;

    @Autowired
    private ChromaService chromaService;

    @Autowired
    private SecurityUserContext securityUserContext;

    private final String UPLOAD_DIR =
            System.getProperty("user.dir")
                    + "/src/main/resources/uploads/";

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file")
            MultipartFile file
    ) {
        try {
            Long userId = securityUserContext.getCurrentUserId();

            if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                return ResponseEntity.badRequest().body("Filename cannot be null or empty");
            }

            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }

            // Using java.nio.file.Paths to get just the file name
            String safeFileName = UUID.randomUUID() + "_" +
                    java.nio.file.Paths.get(originalFilename).getFileName().toString();

            String filePath = UPLOAD_DIR + safeFileName;
            File savedFile = new File(filePath);

            file.transferTo(savedFile);

        // SAVE DB
        AIDocument doc = new AIDocument();
        doc.setFileName(safeFileName);
        doc.setFileType(file.getContentType());
        doc.setFilePath(filePath);
        doc.setUserId(userId);
        repository.save(doc);

        // EXTRACT TEXT

        String extractedText = "";

        // PDF

        if (file.getOriginalFilename()
                .endsWith(".pdf")) {

            PDDocument pdf =
                    PDDocument.load(savedFile);

            PDFTextStripper stripper =
                    new PDFTextStripper();

            extractedText =
                    stripper.getText(pdf);

            pdf.close();
        }

        // DOCX

        else if (file.getOriginalFilename()
                .endsWith(".docx")) {

            XWPFDocument document =
                    new XWPFDocument(
                            new FileInputStream(savedFile)
                    );

            XWPFWordExtractor extractor =
                    new XWPFWordExtractor(
                            document
                    );

            extractedText =
                    extractor.getText();

            extractor.close();
        }

        // TXT

        else {

            extractedText =
                    Files.readString(
                            savedFile.toPath()
                    );
        }

        // CHUNK TEXT & STORE EMBEDDINGS
        try {
            List<String> chunks = chunkService.chunkText(extractedText, 1000);
            for (String chunk : chunks) {
                List<Double> embedding = embeddingService.createEmbedding(chunk);
                chromaService.storeChunk(UUID.randomUUID().toString(), chunk, embedding, userId);
            }
        } catch (Exception ex) {
            System.err.println("Warning: Document uploaded but indexing failed: " + ex.getMessage());
        }

        return ResponseEntity.ok("File uploaded and indexed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload and index document: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AIDocument>> getDocuments() {
        Long userId = securityUserContext.getCurrentUserId();
        return ResponseEntity.ok(repository.findByUserId(userId));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleDocument(
            @PathVariable Long id
    ) {
        try {
            Long userId = securityUserContext.getCurrentUserId();
            AIDocument doc = repository.findByIdAndUserId(id, userId)
                            .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Document not found or access denied"));

            doc.setIncluded(!doc.isIncluded());
            repository.save(doc);
            return ResponseEntity.ok(doc);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id
    ) {
        try {
            Long userId = securityUserContext.getCurrentUserId();
            AIDocument doc = repository.findByIdAndUserId(id, userId)
                            .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Document not found or access denied"));

            // Delete from file system
            try {
                File file = new File(doc.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Delete from database
            repository.delete(doc);
            return ResponseEntity.ok("Document deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
