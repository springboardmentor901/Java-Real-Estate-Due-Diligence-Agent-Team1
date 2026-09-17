package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.entity.ComparableListing;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.repository.ComparableListingRepository;
import com.realestate.due_diligence_agent.repository.RiskAssessmentRepository;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelReportGenerator {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ComparableListingRepository comparableListingRepository;
    private final PropertyTimelineBuilder propertyTimelineBuilder;

    public void generateReport(
            Report report,
            String filePath) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            // ---------------------------------------------
            // 1. SUMMARY SHEET
            // ---------------------------------------------

            createSummarySheet(workbook, report);

            // ---------------------------------------------
            // 2. RISK ASSESSMENT SHEET
            // ---------------------------------------------

            createRiskAssessmentSheet(workbook, report);

            // ---------------------------------------------
            // 3. COMPARABLE LISTINGS SHEET
            // ---------------------------------------------

            createComparableListingsSheet(workbook, report);

            // ---------------------------------------------
            // 4. TIMELINE SHEET
            // ---------------------------------------------

            createTimelineSheet(workbook, report);

            // ---------------------------------------------
            // WRITE WORKBOOK
            // ---------------------------------------------

            try (FileOutputStream outputStream =
                         new FileOutputStream(filePath)) {

                workbook.write(outputStream);
            }
        }
    }

    // =====================================================
    // SUMMARY
    // =====================================================

    private void createSummarySheet(
            Workbook workbook,
            Report report) {

        Sheet sheet = workbook.createSheet("Summary");

        createHeader(
                sheet,
                0,
                "Property Due Diligence Summary"
        );

        int rowNumber = 2;

        addKeyValue(
                sheet,
                rowNumber++,
                "Report ID",
                safe(report.getId())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Property ID",
                safe(report.getProperty().getId())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Address",
                safe(report.getProperty().getAddress())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Property Type",
                safe(report.getProperty().getPropertyType())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Bedrooms",
                safe(report.getProperty().getBedrooms())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Bathrooms",
                safe(report.getProperty().getBathrooms())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Square Feet",
                safe(report.getProperty().getSquareFeet())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Year Built",
                safe(report.getProperty().getYearBuilt())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Overall Risk Score",
                safe(report.getRiskScore())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Status",
                safe(report.getStatus())
        );

        addKeyValue(
                sheet,
                rowNumber++,
                "Created At",
                safe(report.getCreatedAt())
        );

        rowNumber++;

        createHeader(
                sheet,
                rowNumber,
                "Executive Summary"
        );

        rowNumber++;

        Row summaryRow = sheet.createRow(rowNumber);

        Cell summaryCell = summaryRow.createCell(0);

        summaryCell.setCellValue(
                report.getExecutiveSummary() != null
                        ? report.getExecutiveSummary()
                        : "Executive summary is not available."
        );

        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        rowNumber,
                        rowNumber + 5,
                        0,
                        3
                )
        );

        summaryCell.setCellStyle(wrapStyle(workbook));

        autoSizeColumns(sheet, 4);
    }

    // =====================================================
    // RISK ASSESSMENT
    // =====================================================

    private void createRiskAssessmentSheet(
            Workbook workbook,
            Report report) {

        Sheet sheet = workbook.createSheet("Risk Assessment");

        createHeader(
                sheet,
                0,
                "Risk Assessment"
        );

        Row headerRow = sheet.createRow(2);

        createCell(headerRow, 0, "Category", true);
        createCell(headerRow, 1, "Indicator", true);
        createCell(headerRow, 2, "Score", true);
        createCell(headerRow, 3, "Notes", true);

        List<RiskAssessment> assessments =
                riskAssessmentRepository.findAll()
                        .stream()
                        .filter(assessment ->
                                assessment.getReport() != null
                                        && assessment.getReport().getId() != null
                                        && assessment.getReport()
                                                .getId()
                                                .equals(report.getId())
                        )
                        .toList();

        int rowNumber = 3;

        for (RiskAssessment assessment : assessments) {

            Row row = sheet.createRow(rowNumber++);

            createCell(
                    row,
                    0,
                    safe(assessment.getCategory()),
                    false
            );

            createCell(
                    row,
                    1,
                    safe(assessment.getIndicator()),
                    false
            );

            createCell(
                    row,
                    2,
                    safe(assessment.getScore()),
                    false
            );

            createCell(
                    row,
                    3,
                    safe(assessment.getNotes()),
                    false
            );
        }

        autoSizeColumns(sheet, 4);
    }

    // =====================================================
    // COMPARABLE LISTINGS
    // =====================================================

    private void createComparableListingsSheet(
            Workbook workbook,
            Report report) {

        Sheet sheet = workbook.createSheet(
                "Comparable Listings"
        );

        createHeader(
                sheet,
                0,
                "Comparable Property Listings"
        );

        Row headerRow = sheet.createRow(2);

        createCell(
                headerRow,
                0,
                "Address",
                true
        );

        createCell(
                headerRow,
                1,
                "Price",
                true
        );

        createCell(
                headerRow,
                2,
                "Square Feet",
                true
        );

        createCell(
                headerRow,
                3,
                "Distance (Miles)",
                true
        );

        createCell(
                headerRow,
                4,
                "Listed Date",
                true
        );

        createCell(
                headerRow,
                5,
                "Source",
                true
        );

        List<ComparableListing> listings =
                comparableListingRepository.findByPropertyId(
                        report.getProperty().getId()
                );

        int rowNumber = 3;

        for (ComparableListing listing : listings) {

            Row row = sheet.createRow(rowNumber++);

            createCell(
                    row,
                    0,
                    safe(listing.getComparableAddress()),
                    false
            );

            // FIXED: Convert Double to String when "N/A"
            // is required for null values.
            createCell(
                    row,
                    1,
                    listing.getPrice() != null
                            ? String.valueOf(listing.getPrice())
                            : "N/A",
                    false
            );

            // FIXED: Convert Double to String when "N/A"
            // is required for null values.
            createCell(
                    row,
                    2,
                    listing.getSquareFeet() != null
                            ? String.valueOf(listing.getSquareFeet())
                            : "N/A",
                    false
            );

            // FIXED: Convert Double to String when "N/A"
            // is required for null values.
            createCell(
                    row,
                    3,
                    listing.getDistanceMiles() != null
                            ? String.valueOf(listing.getDistanceMiles())
                            : "N/A",
                    false
            );

            createCell(
                    row,
                    4,
                    safe(listing.getListedDate()),
                    false
            );

            createCell(
                    row,
                    5,
                    safe(listing.getSource()),
                    false
            );
        }

        autoSizeColumns(sheet, 6);
    }

    // =====================================================
    // TIMELINE
    // =====================================================

    private void createTimelineSheet(
            Workbook workbook,
            Report report) {

        Sheet sheet = workbook.createSheet("Timeline");

        createHeader(
                sheet,
                0,
                "Property Timeline"
        );

        Row headerRow = sheet.createRow(2);

        createCell(
                headerRow,
                0,
                "Date",
                true
        );

        createCell(
                headerRow,
                1,
                "Event",
                true
        );

        createCell(
                headerRow,
                2,
                "Description",
                true
        );

        var timelineEntries =
                propertyTimelineBuilder.build(
                        report.getProperty()
                );

        int rowNumber = 3;

        if (timelineEntries != null) {

            for (var entry : timelineEntries) {

                Row row = sheet.createRow(rowNumber++);

                createCell(
                        row,
                        0,
                        safe(entry.getDate()),
                        false
                );

                createCell(
                        row,
                        1,
                        safe(entry.getLabel()),
                        false
                );

                createCell(
                        row,
                        2,
                        safe(entry.getDescription()),
                        false
                );
            }
        }

        autoSizeColumns(sheet, 3);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private void createHeader(
            Sheet sheet,
            int rowNumber,
            String title) {

        Row row = sheet.createRow(rowNumber);

        Cell cell = row.createCell(0);

        cell.setCellValue(title);

        CellStyle style =
                sheet.getWorkbook().createCellStyle();

        Font font =
                sheet.getWorkbook().createFont();

        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        style.setFont(font);

        cell.setCellStyle(style);
    }

    private void addKeyValue(
            Sheet sheet,
            int rowNumber,
            String key,
            String value) {

        Row row = sheet.createRow(rowNumber);

        Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }

    private void createCell(
            Row row,
            int column,
            String value,
            boolean bold) {

        Cell cell = row.createCell(column);

        cell.setCellValue(value);

        if (bold) {

            CellStyle style =
                    row.getSheet()
                            .getWorkbook()
                            .createCellStyle();

            Font font =
                    row.getSheet()
                            .getWorkbook()
                            .createFont();

            font.setBold(true);

            style.setFont(font);

            cell.setCellStyle(style);
        }
    }

    private void createCell(
            Row row,
            int column,
            double value,
            boolean bold) {

        Cell cell = row.createCell(column);

        cell.setCellValue(value);

        if (bold) {

            CellStyle style =
                    row.getSheet()
                            .getWorkbook()
                            .createCellStyle();

            Font font =
                    row.getSheet()
                            .getWorkbook()
                            .createFont();

            font.setBold(true);

            style.setFont(font);

            cell.setCellStyle(style);
        }
    }

    private CellStyle wrapStyle(Workbook workbook) {

        CellStyle style = workbook.createCellStyle();

        style.setWrapText(true);

        style.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        return style;
    }

    private void autoSizeColumns(
            Sheet sheet,
            int numberOfColumns) {

        for (int i = 0; i < numberOfColumns; i++) {

            sheet.autoSizeColumn(i);

            if (sheet.getColumnWidth(i) > 12000) {
                sheet.setColumnWidth(i, 12000);
            }
        }
    }

    private String safe(Object value) {

        return value == null
                ? "N/A"
                : String.valueOf(value);
    }
}