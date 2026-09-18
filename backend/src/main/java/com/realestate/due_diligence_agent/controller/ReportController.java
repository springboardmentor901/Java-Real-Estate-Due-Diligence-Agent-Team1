package com.realestate.due_diligence_agent.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.ReportResponse;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.service.FileStorageService;
import com.realestate.due_diligence_agent.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final FileStorageService fileStorageService;

    // =====================================================
    // CREATE REPORT
    // =====================================================

    @PostMapping("/{id}/reports")
    public ResponseEntity<ReportResponse> createReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        Report report =
                reportService.generateReport(
                        id,
                        user
                );

        ReportResponse response =
                new ReportResponse(
                        report.getId()
                );

        return ResponseEntity.ok(response);
    }
    // =====================================================
// GET REPORT DETAILS
// =====================================================

@GetMapping("/{propertyId}/reports/{reportId}")
public ResponseEntity<Report> getReport(
        @PathVariable Long propertyId,
        @PathVariable Long reportId,
        @AuthenticationPrincipal User user) {

    Report report =
            reportService.getReportById(reportId);

    // Make sure the report belongs to the requested property
    if (!report.getProperty().getId().equals(propertyId)) {
        return ResponseEntity.notFound().build();
    }

    // Make sure the authenticated user owns the report
    if (!report.getRequestedBy().getId().equals(user.getId())) {
        return ResponseEntity.status(403).build();
    }

    return ResponseEntity.ok(report);
}

    // =====================================================
    // DOWNLOAD PDF
    // =====================================================

    @GetMapping("/{propertyId}/reports/{reportId}/pdf")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable Long propertyId,
            @PathVariable Long reportId,
            @AuthenticationPrincipal User user) {

        Report report =
                reportService.getReportById(
                        reportId
                );

        // Make sure the report belongs to the requested property
        if (!report.getProperty().getId().equals(propertyId)) {
            return ResponseEntity.notFound().build();
        }

        // Make sure the authenticated user owns the report
        if (!report.getRequestedBy().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (report.getPdfUrl() == null ||
                report.getPdfUrl().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        Resource resource =
                fileStorageService.loadFile(
                        report.getPdfUrl()
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-"
                                + reportId
                                + ".pdf\""
                )
                .body(resource);
    }

    // =====================================================
    // DOWNLOAD EXCEL
    // =====================================================

    @GetMapping("/{propertyId}/reports/{reportId}/excel")
    public ResponseEntity<Resource> downloadExcel(
            @PathVariable Long propertyId,
            @PathVariable Long reportId,
            @AuthenticationPrincipal User user) {

        Report report =
                reportService.getReportById(
                        reportId
                );

        // Make sure the report belongs to the requested property
        if (!report.getProperty().getId().equals(propertyId)) {
            return ResponseEntity.notFound().build();
        }

        // Make sure the authenticated user owns the report
        if (!report.getRequestedBy().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (report.getExcelUrl() == null ||
                report.getExcelUrl().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        Resource resource =
                fileStorageService.loadFile(
                        report.getExcelUrl()
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-"
                                + reportId
                                + ".xlsx\""
                )
                .body(resource);
    }
}