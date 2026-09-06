package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.entity.ZoningInformation;

@Service
public class ZoningComplianceStrategy implements RiskCategoryStrategy {

    @Override
    public RiskAssessment assess(Property property, Report report) {

        ZoningInformation zoning = property.getZoningInformation();

        int score;
        String indicator;
        String notes;

        if (zoning == null) {

            score = 0;
            indicator = "ZONING_DATA_UNAVAILABLE";
            notes = "No zoning information available for analysis.";

        } else if (Boolean.FALSE.equals(zoning.getZoningCompliance())) {

            score = 100;
            indicator = "ZONING_NON_COMPLIANT";
            notes = "Property zoning information indicates non-compliance.";

        } else if (Boolean.TRUE.equals(zoning.getZoningCompliance())) {

            score = 0;
            indicator = "ZONING_COMPLIANT";
            notes = "Property zoning information indicates compliance.";

        } else {

            score = 50;
            indicator = "ZONING_COMPLIANCE_UNKNOWN";
            notes = buildUnknownComplianceNotes(zoning);
        }

        return RiskAssessment.builder()
                .report(report)
                .category("ZONING")
                .indicator(indicator)
                .score(score)
                .notes(notes)
                .build();
    }

    private String buildUnknownComplianceNotes(ZoningInformation zoning) {

        StringBuilder notes = new StringBuilder(
                "Zoning compliance could not be determined."
        );

        if (zoning.getLandUse() != null) {
            notes.append(" Land use: ")
                    .append(zoning.getLandUse())
                    .append(".");
        }

        if (zoning.getSetbackRequirements() != null) {
            notes.append(" Setback requirements: ")
                    .append(zoning.getSetbackRequirements())
                    .append(".");
        }

        return notes.toString();
    }
}