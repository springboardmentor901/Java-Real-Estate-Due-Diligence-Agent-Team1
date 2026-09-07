package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;

@Service
public class LegalRiskStrategy implements RiskCategoryStrategy {

    @Override
    public RiskAssessment assess(Property property, Report report) {

        return RiskAssessment.builder()
                .report(report)
                .category("LEGAL")
                .indicator("LOW_LEGAL_RISK")
                .score(0)
                .notes("No significant legal risk indicators were identified.")
                .build();
    }
}