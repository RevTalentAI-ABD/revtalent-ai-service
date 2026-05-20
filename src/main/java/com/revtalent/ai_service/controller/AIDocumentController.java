package com.revtalent.ai_service.controller;

import com.revtalent.ai_service.model.AIDocument;

import com.revtalent.ai_service.repository.AIDocumentRepository;

import com.revtalent.ai_service.service.ChromaService;
import com.revtalent.ai_service.service.OllamaEmbeddingService;
import com.revtalent.ai_service.service.TextChunkService;

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
@CrossOrigin("*")
public class AIDocumentController {

    @Autowired
    private AIDocumentRepository repository;

    @Autowired
    private TextChunkService chunkService;

    @Autowired
    private OllamaEmbeddingService embeddingService;

    @Autowired
    private ChromaService chromaService;

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
    ) throws IOException {

        File dir = new File(UPLOAD_DIR);

        if (!dir.exists()) {

            dir.mkdirs();
        }

        String filePath =
                UPLOAD_DIR +
                        file.getOriginalFilename();

        File savedFile =
                new File(filePath);

        file.transferTo(savedFile);

        // SAVE DB

        AIDocument doc =
                new AIDocument();

        doc.setFileName(
                file.getOriginalFilename()
        );

        doc.setFileType(
                file.getContentType()
        );

        doc.setFilePath(filePath);

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

        // CHUNK TEXT

        List<String> chunks =
                chunkService.chunkText(
                        extractedText,
                        1000
                );

        // STORE EMBEDDINGS

        for (String chunk : chunks) {

            List<Double> embedding =
                    embeddingService
                            .createEmbedding(
                                    chunk
                            );

            chromaService.storeChunk(

                    UUID.randomUUID()
                            .toString(),

                    chunk,

                    embedding
            );
        }

        return ResponseEntity.ok(
                "File uploaded and indexed successfully"
        );
    }

    @GetMapping
    public List<AIDocument> getDocuments() {

        return repository.findAll();
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleDocument(
            @PathVariable Long id
    ) {

        AIDocument doc =
                repository.findById(id)
                        .orElseThrow();

        doc.setIncluded(
                !doc.isIncluded()
        );

        repository.save(doc);

        return ResponseEntity.ok(doc);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id
    ) {

        AIDocument doc =
                repository.findById(id)
                        .orElseThrow();

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
    }
}
