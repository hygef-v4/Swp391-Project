package org.swp391.hotelbookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.swp391.hotelbookingsystem.repository.LocationRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class HotelBookingSystemApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(HotelBookingSystemApplication.class, args);

        try {
            DataSource dataSource = context.getBean(DataSource.class);
            try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
                System.out.println("Database URL: " + connection.getMetaData().getURL());
                System.out.println("JDBC connection is successful!");
            }
        } catch (SQLException e) {
            System.err.println("Error while testing JDBC connection: " + e.getMessage());
        }


    }


}
