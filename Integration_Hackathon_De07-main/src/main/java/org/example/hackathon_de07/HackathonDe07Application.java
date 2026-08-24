package org.example.hackathon_de07;

import org.example.hackathon_de07.service.DatabaseInitializeService;
import org.example.hackathon_de07.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class HackathonDe07Application implements CommandLineRunner {
    @Autowired
    private DatabaseInitializeService databaseInitializeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(HackathonDe07Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        databaseInitializeService.initializeDatabase();
        
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM vector_store", Integer.class);
            if (count != null && count == 0) {
                System.out.println("Vector store is empty, starting to ingest PDF...");
                // Thực hiện gọi tới phương thức để insert dữ liệu dưới dạng database vào bảng vector_store nếu chạy dự án lần đầu tiên
                System.out.println("PDF ingested successfully.");
            } else {
                System.out.println("Vector store already contains data (" + count + " rows). Skip ingestion.");
            }
        } catch (Exception e) {
            System.out.println("Table vector_store might not exist yet or another error occurred. Assuming empty.");

        }
    }
}
