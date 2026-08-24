package org.example.hackathon_de07.controller;

import org.example.hackathon_de07.service.PdfIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final PdfIngestionService pdfIngestionService;

    public AdminController(PdfIngestionService pdfIngestionService) {
        this.pdfIngestionService = pdfIngestionService;
    }

    @PostMapping("/ingest-store-info")
    public ResponseEntity<String> ingestStoreInfo() {
        try {
            pdfIngestionService.ingestPdf();
            return ResponseEntity.ok("PDF ingested successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}