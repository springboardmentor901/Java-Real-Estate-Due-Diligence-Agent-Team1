package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.client.RegridClient;
import com.realestate.due_diligence_agent.dto.RegridParcelResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.ZoningInformation;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.ZoningInformationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ZoningService {

    private final ZoningInformationRepository zoningInformationRepository;
    private final PropertyRepository propertyRepository;
    private final RegridClient regridClient;

    public ZoningService(ZoningInformationRepository zoningInformationRepository,
                         PropertyRepository propertyRepository,
                         RegridClient regridClient) {
        this.zoningInformationRepository = zoningInformationRepository;
        this.propertyRepository = propertyRepository;
        this.regridClient = regridClient;
    }

    public List<ZoningInformation> getZoningInformation(Long propertyId) {
        List<ZoningInformation> existing = zoningInformationRepository.findByPropertyId(propertyId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Property property = propertyOpt.get();
        List<ZoningInformation> newRecords = new ArrayList<>();

        if (property.getLatitude() != null && property.getLongitude() != null) {
            try {
                RegridParcelResponse response = regridClient.getParcelInfo(property.getLatitude().toString(), property.getLongitude().toString());
                if (response != null) {
                    ZoningInformation record = ZoningInformation.builder()
                            .property(property)
                            .zoningClassification(response.getZoningCode())
                            .landUse(response.getLandUseDesc())
                            .source("Regrid")
                            .build();
                    newRecords.add(record);
                }
            } catch (Exception e) {
                // log and continue
            }
        }

        if (!newRecords.isEmpty()) {
            zoningInformationRepository.saveAll(newRecords);
        }
        return newRecords;
    }
}
