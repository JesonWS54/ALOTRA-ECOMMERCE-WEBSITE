package com.alotra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Application Class for AloTra Milk Tea Website
 * 
 * @author AloTra Development Team
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
@EnableJpaAuditing
public class AlotraWebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlotraWebappApplication.class, args);
        System.out.println("==============================================");
        System.out.println("🧋 AloTra Milk Tea Application Started!");
        System.out.println("==============================================");
        System.out.println("📍 Server: http://localhost:8080");
        System.out.println("📍 API Base: http://localhost:8080/api");
        System.out.println("📍 Admin: http://localhost:8080/admin");
        System.out.println("==============================================");
    }

}