package com.realestate.due_diligence_agent.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.repository.RiskAssessmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecutiveSummaryGenerator {

    private final RiskAssessmentRepository riskAssessmentRepository;

    public String generate(Report report) {

        List<RiskAssessment> assessments =
                riskAssessmentRepository.findAll()
                        .stream()
                        .filter(assessment ->
                                assessment.getReport() != null
                                && assessment.getReport().getId().equals(report.getId()))
                        .collect(Collectors.toList());

        String riskLevel = determineRiskLevel(report.getRiskScore());

        String categorySummary = buildCategorySummary(assessments);

        String comparableSummary =
                "Comparable property/listing data is not currently available.";

        return "Executive Summary\n\n"
                + "Overall Risk Score: "
                + (report.getRiskScore() != null
                    ? report.getRiskScore()
                    : "Not available")
                + ".\n"
                + "Overall Risk Level: "
                + riskLevel
                + ".\n\n"
                + "Risk Category Results:\n"
                + categorySummary
                + "\nComparable Property/Listing Summary:\n"
                + comparableSummary;
    }

    private String determineRiskLevel(Integer riskScore) {

        if (riskScore == null) {
            return "NOT AVAILABLE";
        }

        if (riskScore >= 70) {
            return "HIGH";
        }

        if (riskScore >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String buildCategorySummary(
            List<RiskAssessment> assessments) {

        if (assessments.isEmpty()) {
            return "No individual risk category results are available.";
        }

        return assessments.stream()
                .map(assessment ->
                        "- "
                        + assessment.getCategory()
                        + ": "
                        + assessment.getIndicator()
                        + " (Score: "
                        + assessment.getScore()
                        + ")")
                .collect(Collectors.joining("\n"));
    }
}