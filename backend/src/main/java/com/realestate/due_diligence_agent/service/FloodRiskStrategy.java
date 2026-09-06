package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.repository.FloodZoneDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FloodRiskStrategy implements RiskCategoryStrategy {

    private final FloodZoneDataRepository floodZoneDataRepository;

    @Override
    public RiskAssessment assess(Property property, Report report) {

        FloodZoneData floodData = floodZoneDataRepository
                .findByPropertyId(property.getId())
                .orElse(null);

        int score;
        String indicator;
        String notes;

        if (floodData == null) {

            score = 0;
            indicator = "FLOOD_DATA_UNAVAILABLE";
            notes = "No flood zone data available for analysis.";

        } else {

            String floodZone = floodData.getFloodZone();
            String riskRating = floodData.getFloodRiskRating();

            String zone = floodZone == null
                    ? ""
                    : floodZone.trim().toUpperCase();

            String rating = riskRating == null
                    ? ""
                    : riskRating.trim().toUpperCase();

            if (isHighRiskZone(zone) || isHighRiskRating(rating)) {

                score = 100;
                indicator = "HIGH_FLOOD_RISK";
                notes = "Property is located in an area with high flood risk.";

            } else if (isMediumRiskZone(zone) || isMediumRiskRating(rating)) {

                score = 50;
                indicator = "MEDIUM_FLOOD_RISK";
                notes = "Property has moderate flood risk indicators.";

            } else {

                score = 0;
                indicator = "LOW_FLOOD_RISK";
                notes = "No significant flood risk indicators were found.";
            }
        }

        return RiskAssessment.builder()
                .report(report)
                .category("FLOOD")
                .indicator(indicator)
                .score(score)
                .notes(notes)
                .build();
    }

    private boolean isHighRiskZone(String zone) {
        return zone.equals("A")
                || zone.equals("AE")
                || zone.equals("AH")
                || zone.equals("AO")
                || zone.equals("V")
                || zone.equals("VE");
    }

    private boolean isMediumRiskZone(String zone) {
        return zone.startsWith("A")
                || zone.startsWith("V");
    }

    private boolean isHighRiskRating(String rating) {
        return rating.contains("HIGH");
    }

    private boolean isMediumRiskRating(String rating) {
        return rating.contains("MODERATE")
                || rating.contains("MEDIUM");
    }
}