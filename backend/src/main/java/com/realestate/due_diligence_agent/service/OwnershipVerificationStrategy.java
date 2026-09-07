package com.realestate.due_diligence_agent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;

@Service
public class OwnershipVerificationStrategy implements RiskCategoryStrategy {

    @Override
    public RiskAssessment assess(Property property, Report report) {

        List<OwnershipRecord> records = property.getOwnershipRecords();

        int score;
        String indicator;
        String notes;

        if (records == null || records.isEmpty()) {

            score = 50;
            indicator = "OWNERSHIP_DATA_UNAVAILABLE";
            notes = "No ownership records are available for verification.";

        } else {

            long suspiciousRecords = records.stream()
                    .filter(this::isSuspicious)
                    .count();

            if (suspiciousRecords > 0) {

                score = 100;
                indicator = "OWNERSHIP_INCONSISTENCY";
                notes = suspiciousRecords
                        + " ownership record(s) contain missing or inconsistent information.";

            } else {

                score = 0;
                indicator = "OWNERSHIP_VERIFIED";
                notes = "Available ownership records contain consistent information.";
            }
        }

        return RiskAssessment.builder()
                .report(report)
                .category("OWNERSHIP")
                .indicator(indicator)
                .score(score)
                .notes(notes)
                .build();
    }

    private boolean isSuspicious(OwnershipRecord record) {

        if (isBlank(record.getOwnerName())
                || isBlank(record.getOwnershipType())
                || record.getAcquisitionDate() == null) {
            return true;
        }

        if (record.getAcquisitionDate().isAfter(java.time.LocalDate.now())) {
            return true;
        }

        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}