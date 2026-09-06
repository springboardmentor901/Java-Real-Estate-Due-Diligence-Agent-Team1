package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.ReportRepository;
import com.realestate.due_diligence_agent.repository.RiskAssessmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskAssessmentService {

    private final PropertyRepository propertyRepository;
    private final ReportRepository reportRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

    private final TaxDueAnalysisStrategy taxDueAnalysisStrategy;
    private final FloodRiskStrategy floodRiskStrategy;
    private final ZoningComplianceStrategy zoningComplianceStrategy;
    private final PermitComplianceStrategy permitComplianceStrategy;
    private final OwnershipVerificationStrategy ownershipVerificationStrategy;
    private final LegalRiskStrategy legalRiskStrategy;

    public RiskAssessment assessTaxDue(Long propertyId, Long reportId) {

        Property property = getProperty(propertyId);
        Report report = getReport(reportId);

        validateReportBelongsToProperty(report, propertyId);

        RiskAssessment assessment =
                taxDueAnalysisStrategy.assess(property, report);

        return riskAssessmentRepository.save(assessment);
    }
    public Report calculateOverallRisk(Long propertyId, Long reportId) {

    Property property = getProperty(propertyId);
    Report report = getReport(reportId);

    validateReportBelongsToProperty(report, propertyId);

    RiskAssessment tax =
            riskAssessmentRepository.save(
                    taxDueAnalysisStrategy.assess(property, report));

    RiskAssessment flood =
            riskAssessmentRepository.save(
                    floodRiskStrategy.assess(property, report));

    RiskAssessment zoning =
            riskAssessmentRepository.save(
                    zoningComplianceStrategy.assess(property, report));

    RiskAssessment permit =
            riskAssessmentRepository.save(
                    permitComplianceStrategy.assess(property, report));

    RiskAssessment ownership =
            riskAssessmentRepository.save(
                    ownershipVerificationStrategy.assess(property, report));

    RiskAssessment legal =
            riskAssessmentRepository.save(
                    legalRiskStrategy.assess(property, report));

    double weightedScore =
            (tax.getScore() * 0.20)
            + (flood.getScore() * 0.20)
            + (zoning.getScore() * 0.15)
            + (permit.getScore() * 0.15)
            + (ownership.getScore() * 0.15)
            + (legal.getScore() * 0.15);

    report.setRiskScore((int) Math.round(weightedScore));

    return reportRepository.save(report);
}

    private Property getProperty(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Property not found with id: " + propertyId));
    }

    private Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Report not found with id: " + reportId));
    }

    private void validateReportBelongsToProperty(
            Report report,
            Long propertyId) {

        if (!report.getProperty().getId().equals(propertyId)) {
            throw new RuntimeException(
                    "Report does not belong to the specified property");
        }
    }
}