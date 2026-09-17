package com.realestate.due_diligence_agent.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import com.realestate.due_diligence_agent.entity.ComparableListing;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.repository.ComparableListingRepository;
import com.realestate.due_diligence_agent.repository.RiskAssessmentRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportGenerator {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ComparableListingRepository comparableListingRepository;
    private final PropertyTimelineBuilder propertyTimelineBuilder;
    

    public void generateReport(Report report, String filePath) throws IOException {

        Property property = report.getProperty();

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // -------------------------------------------------
        // TITLE
        // -------------------------------------------------

        document.add(
                new Paragraph("REAL ESTATE DUE DILIGENCE REPORT")
                        .setBold()
                        .setFontSize(20)
        );

        document.add(
                new Paragraph(
                        "Report ID: " + safe(report.getId())
                                + "    |    Status: " + safe(report.getStatus())
                )
        );

        document.add(new Paragraph("\n"));

        // -------------------------------------------------
        // PROPERTY INFORMATION
        // -------------------------------------------------

        document.add(
                new Paragraph("1. Property Information")
                        .setBold()
                        .setFontSize(16)
        );

        Table propertyTable = new Table(UnitValue.createPercentArray(2))
                .useAllAvailableWidth();

        addRow(propertyTable, "Property ID", safe(property.getId()));
        addRow(propertyTable, "Address", safe(property.getAddress()));
        addRow(propertyTable, "Property Type", safe(property.getPropertyType()));
        addRow(propertyTable, "Bedrooms", safe(property.getBedrooms()));
        addRow(propertyTable, "Bathrooms", safe(property.getBathrooms()));
        addRow(propertyTable, "Square Feet", safe(property.getSquareFeet()));
        addRow(propertyTable, "Year Built", safe(property.getYearBuilt()));

        document.add(propertyTable);

        document.add(new Paragraph("\n"));

        // -------------------------------------------------
        // EXECUTIVE SUMMARY
        // -------------------------------------------------

        document.add(
                new Paragraph("2. Executive Summary")
                        .setBold()
                        .setFontSize(16)
        );

        document.add(
                new Paragraph(
                        report.getExecutiveSummary() != null
                                ? report.getExecutiveSummary()
                                : "Executive summary is not available."
                )
        );

        document.add(new Paragraph("\n"));

        // -------------------------------------------------
        // RISK ASSESSMENT
        // -------------------------------------------------

        document.add(
                new Paragraph("3. Risk Assessment")
                        .setBold()
                        .setFontSize(16)
        );

        addRiskSummary(document, report);

        document.add(new Paragraph("\n"));

        // -------------------------------------------------
        // PROPERTY TIMELINE
        // -------------------------------------------------

        document.add(
                new Paragraph("4. Property Timeline")
                        .setBold()
                        .setFontSize(16)
        );

        addTimeline(document, property);

        document.add(new Paragraph("\n"));

        // -------------------------------------------------
        // COMPARABLE LISTINGS
        // -------------------------------------------------

        document.add(
                new Paragraph("5. Comparable Listings")
                        .setBold()
                        .setFontSize(16)
        );

        addComparableListings(document, property);

        document.close();
    }

    private void addRiskSummary(Document document, Report report) {

        document.add(
                new Paragraph(
                        "Overall Risk Score: "
                                + safe(report.getRiskScore())
                )
        );

        List<RiskAssessment> assessments =
                riskAssessmentRepository.findAll()
                        .stream()
                        .filter(a ->
                                a.getReport() != null
                                        && a.getReport().getId() != null
                                        && a.getReport().getId()
                                        .equals(report.getId())
                        )
                        .toList();

        if (assessments.isEmpty()) {
            document.add(
                    new Paragraph(
                            "No individual risk assessments are available."
                    )
            );
            return;
        }

        Table table =
                new Table(UnitValue.createPercentArray(4))
                        .useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(
                new Paragraph("Category").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Indicator").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Score").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Notes").setBold()
        ));

        for (RiskAssessment assessment : assessments) {

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(assessment.getCategory()))
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(assessment.getIndicator()))
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(assessment.getScore()))
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(assessment.getNotes()))
                    )
            );
        }

        document.add(table);
    }

    private void addTimeline(
            Document document,
            Property property) {

        var timelineEntries =
                propertyTimelineBuilder.build(property);

        if (timelineEntries == null || timelineEntries.isEmpty()) {

            document.add(
                    new Paragraph(
                            "No property timeline information is available."
                    )
            );

            return;
        }

        Table table =
                new Table(UnitValue.createPercentArray(3))
                        .useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(
                new Paragraph("Date").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Event").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Description").setBold()
        ));

        timelineEntries.forEach(entry -> {

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(entry.getDate()))
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(entry.getLabel()))
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(safe(entry.getDescription()))
                    )
            );
        });

        document.add(table);
    }

    private void addComparableListings(
            Document document,
            Property property) {

        List<ComparableListing> listings =
                comparableListingRepository.findByPropertyId(
                        property.getId()
                );

        if (listings.isEmpty()) {

            document.add(
                    new Paragraph(
                            "No comparable listings are available."
                    )
            );

            return;
        }

        Table table =
                new Table(UnitValue.createPercentArray(5))
                        .useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(
                new Paragraph("Address").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Price").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Square Feet").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Distance (miles)").setBold()
        ));

        table.addHeaderCell(new Cell().add(
                new Paragraph("Listed Date").setBold()
        ));

        for (ComparableListing listing : listings) {

            table.addCell(
                    new Cell().add(
                            new Paragraph(
                                    safe(listing.getComparableAddress())
                            )
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(
                                    formatPrice(listing.getPrice())
                            )
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(
                                    safe(listing.getSquareFeet())
                            )
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(
                                    safe(listing.getDistanceMiles())
                            )
                    )
            );

            table.addCell(
                    new Cell().add(
                            new Paragraph(
                                    safe(listing.getListedDate())
                            )
                    )
            );
        }

        document.add(table);
    }

    private void addRow(
            Table table,
            String label,
            String value) {

        table.addCell(
                new Cell().add(
                        new Paragraph(label).setBold()
                )
        );

        table.addCell(
                new Cell().add(
                        new Paragraph(value)
                )
        );
    }

    private String formatPrice(Double price) {

        if (price == null) {
            return "N/A";
        }

        return String.format("$%,.2f", price);
    }

    private String safe(Object value) {

        return value == null
                ? "N/A"
                : String.valueOf(value);
    }
}