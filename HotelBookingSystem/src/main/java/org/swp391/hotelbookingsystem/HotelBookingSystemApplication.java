package org.swp391.hotelbookingsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelBookingSystemApplication implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(HotelBookingSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("SELECT 1");
            System.out.println("✅ JDBC connection test successful.");
        } catch (Exception e) {
            System.err.println("❌ JDBC connection test failed: " + e.getMessage());
        }
    }
}
