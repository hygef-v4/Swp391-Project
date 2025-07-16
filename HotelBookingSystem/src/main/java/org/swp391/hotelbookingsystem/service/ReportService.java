package org.swp391.hotelbookingsystem.service;

import org.swp391.hotelbookingsystem.model.Report;
import org.swp391.hotelbookingsystem.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public void flagUser(int reporterId, int reportedUserId, String reason) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .reportedUserId(reportedUserId)
                .reason(reason)
                .status("pending")
                .createdAt(java.time.LocalDateTime.now())
                .build();
        reportRepository.createReport(report);
    }

    public void unflagUser(int userId) {
        reportRepository.declinePendingReportsByUserId(userId);
    }

    public boolean isUserFlagged(int userId) {
        return reportRepository.hasPendingReport(userId);
    }

    public Report getLatestPendingReport(int userId) {
        return reportRepository.getLatestPendingReport(userId);
    }

    public List<Report> getAllPendingReports() {
        return reportRepository.findAllPendingReports();
    }

    public void acceptUserReports(int userId) {
        reportRepository.acceptPendingReportsByUserId(userId);
    }
} 