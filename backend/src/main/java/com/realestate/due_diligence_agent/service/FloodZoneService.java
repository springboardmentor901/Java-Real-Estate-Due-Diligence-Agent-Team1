package com.realestate.due_diligence_agent.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.FloodZoneDataRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

import tools.jackson.databind.JsonNode;

@Service
public class FloodZoneService {

    private final FloodZoneDataRepository floodZoneDataRepository;
    private final PropertyRepository propertyRepository;
    private final FemaService femaService;
    private final ElevationService elevationService;

    public FloodZoneService(
            FloodZoneDataRepository floodZoneDataRepository,
            PropertyRepository propertyRepository,
            FemaService femaService,
            ElevationService elevationService) {

        this.floodZoneDataRepository = floodZoneDataRepository;
        this.propertyRepository = propertyRepository;
        this.femaService = femaService;
        this.elevationService = elevationService;
    }

    @Transactional
    public FloodZoneData getFloodZoneData(Long propertyId) {

        // =====================================================
        // 1. DATABASE-FIRST CHECK
        // =====================================================

        Optional<FloodZoneData> existingData =
                floodZoneDataRepository.findByPropertyId(propertyId);

        if (existingData.isPresent()) {
            return existingData.get();
        }


        // =====================================================
        // 2. FIND PROPERTY
        // =====================================================

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + propertyId
                        )
                );


        // =====================================================
        // 3. VALIDATE COORDINATES
        // =====================================================

        if (property.getLatitude() == null ||
                property.getLongitude() == null) {

            throw new RuntimeException(
                    "Property does not have valid latitude and longitude"
            );
        }

        double latitude = property.getLatitude();
        double longitude = property.getLongitude();


        // =====================================================
        // 4. CALL FEMA FLOOD HAZARD ZONE
        // =====================================================

        JsonNode floodResponse =
                femaService.getFloodZone(latitude, longitude);


        // =====================================================
        // 5. CALL FEMA FIRM PANEL
        // =====================================================

        JsonNode panelResponse =
                femaService.getFirmPanel(latitude, longitude);


        // =====================================================
        // 6. CALL ELEVATION SERVICE
        // =====================================================

        Double elevation =
                elevationService.getElevation(latitude, longitude);


        // =====================================================
        // 7. EXTRACT FEMA FLOOD ZONE
        // =====================================================

        String floodZone = null;
        String floodRiskRating = null;

        if (floodResponse != null &&
                floodResponse.get("features") != null &&
                floodResponse.get("features").isArray() &&
                !floodResponse.get("features").isEmpty()) {

            JsonNode attributes =
                    floodResponse
                            .get("features")
                            .get(0)
                            .get("attributes");

            if (attributes != null) {

                JsonNode floodZoneNode =
                        attributes.get("FLD_ZONE");

                if (floodZoneNode != null &&
                        !floodZoneNode.isNull()) {

                    floodZone = floodZoneNode.asText();
                }


                // Use FEMA SFHA indicator as the initial risk
                // classification for the ER field.
                JsonNode sfhaNode =
                        attributes.get("SFHA_TF");

                if (sfhaNode != null &&
                        !sfhaNode.isNull()) {

                    String sfha =
                            sfhaNode.asText();

                    if ("T".equalsIgnoreCase(sfha) ||
                            "Y".equalsIgnoreCase(sfha)) {

                        floodRiskRating = "HIGH";

                    } else if ("F".equalsIgnoreCase(sfha) ||
                            "N".equalsIgnoreCase(sfha)) {

                        floodRiskRating = "LOW";
                    }
                }
            }
        }


        // =====================================================
        // 8. EXTRACT FEMA FIRM PANEL
        // =====================================================

        String femaMapPanel = null;

        if (panelResponse != null &&
                panelResponse.get("features") != null &&
                panelResponse.get("features").isArray() &&
                !panelResponse.get("features").isEmpty()) {

            JsonNode attributes =
                    panelResponse
                            .get("features")
                            .get(0)
                            .get("attributes");

            if (attributes != null) {

                // FEMA panel responses can contain several
                // identifiers. Try the common panel fields.
                JsonNode panelNode =
                        attributes.get("FIRM_PAN");

                if (panelNode == null ||
                        panelNode.isNull()) {

                    panelNode =
                            attributes.get("FIRM_PAN_ID");
                }

                if (panelNode == null ||
                        panelNode.isNull()) {

                    panelNode =
                            attributes.get("FIRM_ID");
                }

                if (panelNode != null &&
                        !panelNode.isNull()) {

                    femaMapPanel =
                            panelNode.asText();
                }
            }
        }


        // =====================================================
        // 9. VALIDATE FEMA RESULT
        // =====================================================

        if (floodZone == null &&
                femaMapPanel == null) {

            throw new RuntimeException(
                    "No FEMA flood-zone information found for property: "
                            + property.getAddress()
            );
        }


        // =====================================================
        // 10. CREATE ENTITY
        // =====================================================

        FloodZoneData floodZoneData =
                FloodZoneData.builder()
                        .property(property)
                        .floodZone(floodZone)
                        .floodRiskRating(floodRiskRating)
                        .femaMapPanel(femaMapPanel)
                        .elevationData(
                                elevation != null
                                        ? BigDecimal.valueOf(elevation)
                                        : null
                        )
                        .retrievedAt(LocalDateTime.now())
                        .build();


        // =====================================================
        // 11. SAVE TO POSTGRESQL
        // =====================================================

        return floodZoneDataRepository.save(floodZoneData);
    }
}
