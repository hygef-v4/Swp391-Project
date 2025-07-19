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
        return jdbcTemplate.query(sql, BANK_MAPPER);
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
                ub.default_account AS default_account
            FROM Banks b
            JOIN UserBanks ub ON b.bank_id = ub.bank_id
            JOIN Users u ON u.user_id = ub.user_id
            WHERE u.user_id = ?
        """;
        return jdbcTemplate.query(sql, BANK_MAPPER, userId);
    }

    public boolean checkBank(int userId, int bankId, String bankNumber){
        String sql = """
            SELECT COUNT(*)
            FROM UserBanks
            WHERE user_id = ? AND bank_id = ? AND bank_number = ?
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, bankId, bankNumber) == 0;
    }

    public boolean isDefault(int userId){
        String sql = """
            SELECT COUNT(*)
            FROM UserBanks
            WHERE user_id = ?
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, userId) == 0;
    }

    public int addBank(int userId, int bankId, String bankNumber, String userName, boolean isDefault){
        String sql;
        if(isDefault){
            sql = """
                INSERT INTO UserBanks (user_id, bank_id, bank_number, user_name)
                VALUES (?, ?, ?, ?)
            """;
        }else{
            sql = """
                INSERT INTO UserBanks (user_id, bank_id, bank_number, user_name, default_account)
                VALUES (?, ?, ?, ?, 0)
            """;
        }return jdbcTemplate.update(sql, userId, bankId, bankNumber, userName);
    }

    public int editBank(int userId, int bankId, String bankNumber, String userName, int oldId, String oldNumber){
        String sql = """
            UPDATE UserBanks SET bank_id = ?, bank_number = ?, user_name = ? 
            WHERE user_id = ? AND bank_id = ? AND bank_number = ?
        """;
        return jdbcTemplate.update(sql, bankId, bankNumber, userName, userId, oldId, oldNumber);
    }

    public int deleteBank(int userId, int bankId){
        String sql = """
            DELETE FROM UserBanks 
            WHERE user_id = ? AND bank_id = ?
        """;
        return jdbcTemplate.update(sql, userId, bankId);
    }
}
