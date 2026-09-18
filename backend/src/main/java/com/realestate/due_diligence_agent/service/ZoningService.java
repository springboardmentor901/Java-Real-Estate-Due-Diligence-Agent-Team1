package com.realestate.due_diligence_agent.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.dto.RegridResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.ZoningInformation;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.ZoningInformationRepository;

@Service
public class ZoningService {

    private final PropertyRepository propertyRepository;

    private final ZoningInformationRepository zoningInformationRepository;

    private final RegridService regridService;


    public ZoningService(
            PropertyRepository propertyRepository,
            ZoningInformationRepository zoningInformationRepository,
            RegridService regridService) {

        this.propertyRepository = propertyRepository;
        this.zoningInformationRepository =
                zoningInformationRepository;
        this.regridService = regridService;
    }


    @Transactional
    public ZoningInformation getZoningInformation(Long propertyId) {

        // =====================================================
        // 1. Find property
        // =====================================================

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: "
                                        + propertyId
                        )
                );


        // =====================================================
        // 2. Check database first
        // =====================================================

        if (property.getZoningInformation() != null
                && property.getZoningInformation().getLandUse() != null
                && property.getZoningInformation().getZoningClassification() != null) {

            return property.getZoningInformation();
        }

        return fetchAndSaveZoningInformation(property);
    }

    @Transactional
    public ZoningInformation refreshZoningInformation(Long propertyId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: "
                                        + propertyId
                        )
                );

        return fetchAndSaveZoningInformation(property);
    }

    private ZoningInformation fetchAndSaveZoningInformation(Property property) {

        // =====================================================
        // 3. Validate coordinates
        // =====================================================

        if (property.getLatitude() == null ||
                property.getLongitude() == null) {

            throw new RuntimeException(
                    "Property does not have valid latitude and longitude"
            );
        }


        // =====================================================
        // 4. Call Regrid
        // =====================================================

        RegridResponse response =
                regridService.getParcelData(
                        property.getLatitude(),
                        property.getLongitude()
                );


        // =====================================================
        // 5. Validate Regrid response
        // =====================================================

        if (response == null) {

            return null;
        }

        if (response.getParcels() == null) {

            return null;
        }

        if (response.getParcels().getFeatures() == null) {

            return null;
        }

        if (response.getParcels().getFeatures().isEmpty()) {

            return null;
        }


        // =====================================================
        // 6. Get first parcel
        // =====================================================

        RegridResponse.Feature parcelFeature =
                response.getParcels()
                        .getFeatures()
                        .get(0);


        // =====================================================
        // 7. Validate properties
        // =====================================================

        if (parcelFeature.getProperties() == null) {

            return null;
        }

        if (parcelFeature.getProperties().getFields() == null) {

            return null;
        }


        // =====================================================
        // 8. Get Regrid fields
        // =====================================================

        RegridResponse.Fields fields =
                parcelFeature.getProperties().getFields();


        // =====================================================
        // 9. Validate zoning
        // =====================================================

        if (fields.getZoning() == null ||
                fields.getZoning().isBlank()) {

            return null;
        }


        // =====================================================
        // 10. Create or update zoning entity
        // =====================================================

        ZoningInformation zoningInformation = property.getZoningInformation();

        if (zoningInformation == null) {
            zoningInformation = ZoningInformation.builder()
                    .property(property)
                    .zoningCode(fields.getZoning())
                    .zoningDescription(fields.getZoningDescription())
                    .landUse(fields.getUsedesc())
                    .zoningClassification(fields.getZoningType())
                    .retrievedAt(LocalDateTime.now())
                    .build();
        } else {
            zoningInformation.setZoningCode(fields.getZoning());
            zoningInformation.setZoningDescription(fields.getZoningDescription());
            zoningInformation.setLandUse(fields.getUsedesc());
            zoningInformation.setZoningClassification(fields.getZoningType());
            zoningInformation.setRetrievedAt(LocalDateTime.now());
        }


        // =====================================================
        // 11. Save zoning information
        // =====================================================

        return zoningInformationRepository.save(
                zoningInformation
        );
    }
}