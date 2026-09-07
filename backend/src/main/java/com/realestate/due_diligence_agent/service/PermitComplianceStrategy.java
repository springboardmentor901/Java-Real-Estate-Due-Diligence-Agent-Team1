package com.realestate.due_diligence_agent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;

@Service
public class PermitComplianceStrategy implements RiskCategoryStrategy {

    @Override
    public RiskAssessment assess(Property property, Report report) {

        List<Permit> permits = property.getPermits();

        int score;
        String indicator;
        String notes;

        if (permits == null || permits.isEmpty()) {

            score = 50;
            indicator = "PERMIT_DATA_UNAVAILABLE";
            notes = "No permit records are available for compliance analysis.";

        } else {

            long incompletePermits = permits.stream()
                    .filter(this::isIncomplete)
                    .count();

            if (incompletePermits > 0) {

                score = 100;
                indicator = "INCOMPLETE_PERMITS";
                notes = incompletePermits
                        + " permit record(s) are missing required information.";

            } else {

                score = 0;
                indicator = "PERMITS_COMPLETE";
                notes = "Available permit records contain the required information.";
            }
        }

        return RiskAssessment.builder()
                .report(report)
                .category("PERMIT")
                .indicator(indicator)
                .score(score)
                .notes(notes)
                .build();
    }

    private boolean isIncomplete(Permit permit) {

        return isBlank(permit.getPermitNumber())
                || isBlank(permit.getPermitType())
                || isBlank(permit.getStatus())
                || permit.getIssuedDate() == null
                || isBlank(permit.getDescription());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}