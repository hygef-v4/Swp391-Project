package org.swp391.hotelbookingsystem.repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Amenity;
import org.swp391.hotelbookingsystem.model.Bank;

@Repository
public class BankRepository {
    private static final BeanPropertyRowMapper<Bank> BANK_MAPPER = new BeanPropertyRowMapper<>(Bank.class);

    private final JdbcTemplate jdbcTemplate;

    public BankRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Bank> getAllBank(){
        String sql = """
            SELECT 
                bank_id AS bankId,
                bank_name AS bankName,
                bank_code AS bankCode,
                logo,
                icon
            FROM Banks
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Bank.class));
    }

    public List<Bank> getUserBanks(int userId){
        String sql = """
            SELECT 
                b.bank_id AS bankId,
                b.bank_name AS bankName,
                b.bank_code AS bankCode,
                b.logo,
                b.icon,
                ub.bank_number AS bankNumber,
                ub.user_name AS userName,
                ub.default_account AS defaultAccount
            FROM Banks b
            JOIN UserBanks ub ON b.bank_id = ub.bank_id
            JOIN Users u ON u.user_id = ub.user_id
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Bank.class));
    }
}
