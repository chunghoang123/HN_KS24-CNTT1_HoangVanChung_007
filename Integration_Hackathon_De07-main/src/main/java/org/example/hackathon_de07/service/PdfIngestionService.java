package org.example.hackathon_de07.service;

import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfIngestionService {

    @Autowired
    private Resource pdfResource;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String INGESTED_FLAG_KEY = "pdf_ingested_flag";

    @PostConstruct
    public void ingestPdf() {
        try {
            if (isAlreadyIngested()) {
                System.out.println("PDF already ingested, skipping...");
                return;
            }

            String content = extractTextFromPdf();
            if (content == null || content.trim().isEmpty()) {
                System.err.println("No text extracted from PDF");
                return;
            }

            List<Document> chunks = chunkText(content);
            System.out.println("Total chunks created: " + chunks.size());

            vectorStore.add(chunks);
            System.out.println("Đã chèn " + chunks.size() + " chunk vào vector_store");

            markAsIngested();

        } catch (Exception e) {
            System.err.println("Lỗi khi nạp PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractTextFromPdf() throws Exception {
        try (InputStream is = pdfResource.getInputStream()) {
            Tika tika = new Tika();
            return tika.parseToString(is);
        }
    }

    private List<Document> chunkText(String text) {
        List<Document> chunks = new ArrayList<>();
        
        // Chiến lược chunking: tách theo các mục số (1., 2., 3., ...) và các đoạn newline đôi
        // Mỗi chunk đại diện cho một mục/chương riêng biệt trong tài liệu
        String[] sections = text.split("(?=\\n\\d+\\.\\s)");
        
        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.length() > 50) {
                // Nếu section quá lớn, tiếp tục tách theo đoạn
                if (trimmed.length() > 1500) {
                    String[] paragraphs = trimmed.split("\\n\\n");
                    StringBuilder currentChunk = new StringBuilder();
                    for (String para : paragraphs) {
                        if (currentChunk.length() + para.length() > 1500) {
                            if (currentChunk.length() > 50) {
                                chunks.add(new Document(currentChunk.toString().trim()));
                            }
                            currentChunk = new StringBuilder(para);
                        } else {
                            if (currentChunk.length() > 0) currentChunk.append("\n\n");
                            currentChunk.append(para);
                        }
                    }
                    if (currentChunk.length() > 50) {
                        chunks.add(new Document(currentChunk.toString().trim()));
                    }
                } else {
                    chunks.add(new Document(trimmed));
                }
            }
        }

        // Fallback: nếu không chunk được thì tách theo đoạn
        if (chunks.isEmpty()) {
            String[] paragraphs = text.split("\\n\\n");
            for (String p : paragraphs) {
                String trimmed = p.trim();
                if (trimmed.length() > 50) {
                    chunks.add(new Document(trimmed));
                }
            }
        }

        return chunks;
    }

    private boolean isAlreadyIngested() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            // Table might not exist yet
            return false;
        }
    }

    private void markAsIngested() {
        // No-op: using vector_store count as the flag
    }
}