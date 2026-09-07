package com.realestate.due_diligence_agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.realestate.due_diligence_agent.client.FloodZoneClient;
import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.FloodZoneDataRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FloodZoneService {

    private final FloodZoneDataRepository floodZoneDataRepository;
    private final PropertyRepository propertyRepository;
    private final FloodZoneClient floodZoneClient;

    public FloodZoneService(FloodZoneDataRepository floodZoneDataRepository,
                            PropertyRepository propertyRepository,
                            FloodZoneClient floodZoneClient) {
        this.floodZoneDataRepository = floodZoneDataRepository;
        this.propertyRepository = propertyRepository;
        this.floodZoneClient = floodZoneClient;
    }

    public List<FloodZoneData> getFloodZoneData(Long propertyId) {
        List<FloodZoneData> existing = floodZoneDataRepository.findByPropertyId(propertyId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Property property = propertyOpt.get();
        List<FloodZoneData> newRecords = new ArrayList<>();

        if (property.getLatitude() != null && property.getLongitude() != null) {
            try {
                JsonNode response = floodZoneClient.getFloodZoneData(property.getLatitude().toString(), property.getLongitude().toString());
                if (response != null && response.has("features") && response.get("features").isArray() && !response.get("features").isEmpty()) {
                    JsonNode feature = response.get("features").get(0);
                    JsonNode attributes = feature.get("attributes");

                    if (attributes != null) {
                        FloodZoneData record = FloodZoneData.builder()
                                .property(property)
                                .floodZone(attributes.has("FLD_ZONE") ? attributes.get("FLD_ZONE").asText() : null)
                                .floodRiskRating(attributes.has("ZONE_SUBTY") ? attributes.get("ZONE_SUBTY").asText() : null)
                                .femaMapPanel(attributes.has("DFIRM_ID") ? attributes.get("DFIRM_ID").asText() : null)
                                .build();
                        newRecords.add(record);
                    }
                }
            } catch (Exception e) {
                // log and continue
            }
        }

        if (!newRecords.isEmpty()) {
            floodZoneDataRepository.saveAll(newRecords);
        }
        return newRecords;
    }
}
