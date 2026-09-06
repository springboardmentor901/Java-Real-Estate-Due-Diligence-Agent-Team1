package com.realestate.due_diligence_agent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.entity.TaxHistory;

@Service
public class TaxDueAnalysisStrategy implements RiskCategoryStrategy {

    @Override
    public RiskAssessment assess(Property property, Report report) {

        List<TaxHistory> taxHistories = property.getTaxHistories();

        int score = 0;
        String notes;

        if (taxHistories == null || taxHistories.isEmpty()) {

            notes = "No tax history available for analysis.";

        } else {

            boolean unpaidTaxExists = taxHistories.stream()
                    .anyMatch(tax -> tax.getPaymentStatus() != null
                            && !tax.getPaymentStatus().equalsIgnoreCase("PAID"));

            if (unpaidTaxExists) {
                score = 100;
                notes = "Unpaid or outstanding property tax found.";
            } else {
                score = 0;
                notes = "No unpaid property tax found.";
            }
        }

        return RiskAssessment.builder()
                .report(report)
                .category("TAX")
                .indicator("TAX_DUE")
                .score(score)
                .notes(notes)
                .build();
    }
}