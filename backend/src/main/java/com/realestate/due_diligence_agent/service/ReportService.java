package com.realestate.due_diligence_agent.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.ReportStatus;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PropertyRepository propertyRepository;

    private final RiskAssessmentService riskAssessmentService;
    private final PropertyTimelineBuilder propertyTimelineBuilder;
    private final ExecutiveSummaryGenerator executiveSummaryGenerator;

    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;

    private final FileStorageService fileStorageService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // =====================================================
    // CREATE REPORT
    // =====================================================

    public Report createReport(
            Long propertyId,
            User requestedBy) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Property not found with id: " + propertyId));

        Report report = Report.builder()
                .property(property)
                .requestedBy(requestedBy)
                .status(ReportStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        return reportRepository.save(report);
    }

    // =====================================================
    // GENERATE REPORT
    // =====================================================

    @Transactional
    public Report generateReport(
            Long propertyId,
            User requestedBy) {

        Report report = createReport(
                propertyId,
                requestedBy
        );

        try {

            // ---------------------------------------------
            // STEP 1: Mark report as IN_PROGRESS
            // ---------------------------------------------

            report.setStatus(
                    ReportStatus.IN_PROGRESS
            );

            reportRepository.save(report);

            // ---------------------------------------------
            // STEP 2: Load property
            // ---------------------------------------------

            Property property =
                    propertyRepository.findById(propertyId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Property not found with id: "
                                                    + propertyId));

            // ---------------------------------------------
            // STEP 3: Calculate overall risk
            // ---------------------------------------------

            report =
                    riskAssessmentService.calculateOverallRisk(
                            propertyId,
                            report.getId()
                    );

            // ---------------------------------------------
            // STEP 4: Build property timeline
            // ---------------------------------------------

            String propertyTimeline =
                    buildPropertyTimelineJson(property);

            report.setPropertyTimeline(
                    propertyTimeline
            );

            // ---------------------------------------------
            // STEP 5: Generate executive summary
            // ---------------------------------------------

            String executiveSummary =
                    executiveSummaryGenerator.generate(
                            report
                    );

            report.setExecutiveSummary(
                    executiveSummary
            );

            // ---------------------------------------------
            // STEP 6: Mark report as COMPLETED
            // ---------------------------------------------

            report.setStatus(
                    ReportStatus.COMPLETED
            );

            // ---------------------------------------------
            // STEP 7: Generate PDF
            // ---------------------------------------------

            String pdfFileName =
                    "report-" + report.getId() + ".pdf";

            String pdfPath =
                    fileStorageService.getFilePath(
                            pdfFileName
                    );

            pdfReportGenerator.generateReport(
                    report,
                    pdfPath
            );

            report.setPdfUrl(
                    pdfPath
            );

            // ---------------------------------------------
            // STEP 8: Generate Excel
            // ---------------------------------------------

            String excelFileName =
                    "report-" + report.getId() + ".xlsx";

            String excelPath =
                    fileStorageService.getFilePath(
                            excelFileName
                    );

            excelReportGenerator.generateReport(
                    report,
                    excelPath
            );

            report.setExcelUrl(
                    excelPath
            );

            // ---------------------------------------------
            // STEP 9: Save final report
            // ---------------------------------------------

            return reportRepository.save(report);

        } catch (Exception ex) {

            // ---------------------------------------------
            // REPORT GENERATION FAILED
            // ---------------------------------------------

            report.setStatus(
                    ReportStatus.FAILED
            );

            String reason =
                    ex.getMessage();

            if (reason == null || reason.isBlank()) {

                reason =
                        "Report generation failed due to an unexpected error.";
            }

            report.setExecutiveSummary(
                    "Report generation failed: "
                            + reason
            );

            return reportRepository.save(report);
        }
    }

    // =====================================================
    // PROPERTY TIMELINE JSON
    // =====================================================

    private String buildPropertyTimelineJson(
            Property property) {

        try {

            return objectMapper.writeValueAsString(
                    propertyTimelineBuilder.build(
                            property
                    )
            );

        } catch (JsonProcessingException ex) {

            throw new RuntimeException(
                    "Failed to build property timeline: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    // =====================================================
    // GET REPORT BY ID
    // =====================================================

    public Report getReportById(
            Long reportId) {

        return reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Report not found with id: "
                                        + reportId));
    }

    // =====================================================
    // GET REPORT HISTORY
    // =====================================================

    public List<Report> getReportsByUser(
            Long userId) {

        return reportRepository.findByRequestedById(
                userId
        );
    }
}