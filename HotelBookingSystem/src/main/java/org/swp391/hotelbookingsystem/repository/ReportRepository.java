package org.swp391.hotelbookingsystem.repository;

import org.swp391.hotelbookingsystem.model.Report;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportRepository {
    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createReport(Report report) {
        String sql = "INSERT INTO Reports (reporter_id, reported_user_id, reason, status, created_at) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, report.getReporterId(), report.getReportedUserId(), report.getReason(), report.getStatus(), report.getCreatedAt());
    }

    public List<Report> findPendingReportsByUserId(int userId) {
        String sql = "SELECT * FROM Reports WHERE reported_user_id = ? AND status = 'pending'";
        return jdbc.query(sql, (rs, rowNum) -> Report.builder()
                .reportId(rs.getInt("report_id"))
                .reporterId(rs.getInt("reporter_id"))
                .reportedUserId(rs.getInt("reported_user_id"))
                .reason(rs.getString("reason"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build(), userId);
    }

    public void declinePendingReportsByUserId(int userId) {
        String sql = "UPDATE Reports SET status = 'declined' WHERE reported_user_id = ? AND status = 'pending'";
        jdbc.update(sql, userId);
    }

    public boolean hasPendingReport(int userId) {
        String sql = "SELECT COUNT(*) FROM Reports WHERE reported_user_id = ? AND status = 'pending'";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public Report getLatestPendingReport(int userId) {
        String sql = "SELECT TOP 1 * FROM Reports WHERE reported_user_id = ? AND status = 'pending' ORDER BY created_at DESC";
        List<Report> reports = jdbc.query(sql, (rs, rowNum) -> Report.builder()
                .reportId(rs.getInt("report_id"))
                .reporterId(rs.getInt("reporter_id"))
                .reportedUserId(rs.getInt("reported_user_id"))
                .reason(rs.getString("reason"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build(), userId);
        return reports.isEmpty() ? null : reports.get(0);
    }

    public List<Report> findAllPendingReports() {
        String sql = "SELECT * FROM Reports WHERE status = 'pending'";
        return jdbc.query(sql, (rs, rowNum) -> Report.builder()
                .reportId(rs.getInt("report_id"))
                .reporterId(rs.getInt("reporter_id"))
                .reportedUserId(rs.getInt("reported_user_id"))
                .reason(rs.getString("reason"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build());
    }

    public void acceptPendingReportsByUserId(int userId) {
        String sql = "UPDATE Reports SET status = 'accepted' WHERE reported_user_id = ? AND status = 'pending'";
        jdbc.update(sql, userId);
    }
} 