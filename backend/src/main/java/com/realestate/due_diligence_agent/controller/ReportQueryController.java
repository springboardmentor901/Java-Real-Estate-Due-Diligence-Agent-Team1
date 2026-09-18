package com.realestate.due_diligence_agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.ReportDetailResponse;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportQueryController {

    private final ReportService reportService;

    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponse> getReport(
            @PathVariable Long id) {

        Report report = reportService.getReportById(id);

        ReportDetailResponse response = new ReportDetailResponse(
                report.getId(),
                report.getProperty().getId(),
                report.getRequestedBy().getId(),
                report.getRiskScore(),
                report.getExecutiveSummary(),
                report.getPropertyTimeline(),
                report.getPdfUrl(),
                report.getExcelUrl(),
                report.getStatus(),
                report.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}