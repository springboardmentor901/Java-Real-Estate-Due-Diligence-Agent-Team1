package com.realestate.due_diligence_agent.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.dto.ShovelsPermitResponse;
import com.realestate.due_diligence_agent.dto.ShovelsPermitResponse.PermitData;
import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.PermitRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

@Service
public class PermitService {

    private final PermitRepository permitRepository;
    private final PropertyRepository propertyRepository;
    private final ShovelsService shovelsService;

    public PermitService(
            PermitRepository permitRepository,
            PropertyRepository propertyRepository,
            ShovelsService shovelsService) {

        this.permitRepository = permitRepository;
        this.propertyRepository = propertyRepository;
        this.shovelsService = shovelsService;
    }

    // =========================================================
    // GET PERMITS
    // =========================================================

    @Transactional
    public List<Permit> getPermits(Long propertyId) {

        // -----------------------------------------------------
        // 1. DATABASE-FIRST CHECK
        // -----------------------------------------------------

        List<Permit> existingPermits =
                permitRepository.findByPropertyId(propertyId);

        if (!existingPermits.isEmpty()) {
            return existingPermits;
        }

        // -----------------------------------------------------
        // 2. FIND PROPERTY
        // -----------------------------------------------------

        Property property =
                propertyRepository.findById(propertyId)
                        .orElseThrow(() ->
                                new PropertyNotFoundException(
                                        "Property not found with id: "
                                                + propertyId
                                )
                        );

        // -----------------------------------------------------
        // 3. VALIDATE ADDRESS
        // -----------------------------------------------------

        if (property.getAddress() == null ||
                property.getAddress().isBlank()) {

            throw new RuntimeException(
                    "Property does not have a valid address"
            );
        }

        // -----------------------------------------------------
        // 4. RESOLVE ADDRESS → GEO ID
        // -----------------------------------------------------

        var addressResponse =
                shovelsService.searchAddress(
                        property.getAddress()
                );

        if (addressResponse == null ||
                addressResponse.get("items") == null ||
                !addressResponse.get("items").isArray() ||
                addressResponse.get("items").isEmpty()) {

            throw new RuntimeException(
                    "No Shovels address match found for property: "
                            + property.getAddress()
            );
        }

        // -----------------------------------------------------
        // 5. FIND BEST GEO ID
        // -----------------------------------------------------

        String geoId = null;

        for (var item : addressResponse.get("items")) {

            String returnedName =
                    item.has("name")
                            ? item.get("name").asText()
                            : null;

            String returnedGeoId =
                    item.has("geo_id")
                            ? item.get("geo_id").asText()
                            : null;

            if (returnedGeoId == null ||
                    returnedGeoId.isBlank()) {

                continue;
            }

            // Prefer exact address match
            if (returnedName != null &&
                    returnedName.equalsIgnoreCase(
                            property.getAddress())) {

                geoId = returnedGeoId;
                break;
            }

            // Otherwise keep first valid geo_id
            if (geoId == null) {
                geoId = returnedGeoId;
            }
        }

        if (geoId == null ||
                geoId.isBlank()) {

            throw new RuntimeException(
                    "Shovels did not return a valid geo_id for property: "
                            + property.getAddress()
            );
        }

        // -----------------------------------------------------
        // 6. PERMIT DATE RANGE
        // -----------------------------------------------------

        String permitFrom = "2020-01-01";

        String permitTo =
                LocalDate.now().toString();

        // -----------------------------------------------------
        // 7. FETCH ALL SHOVELS PAGES
        // -----------------------------------------------------

        List<Permit> permits =
                new ArrayList<>();

        String cursor = null;

        do {

            ShovelsPermitResponse response =
                    shovelsService.searchPermits(
                            geoId,
                            permitFrom,
                            permitTo,
                            null,
                            100,
                            cursor
                    );

            if (response == null) {
                break;
            }

            // -------------------------------------------------
            // 8. MAP CURRENT PAGE
            // -------------------------------------------------

            if (response.getItems() != null) {

                for (PermitData permitData :
                        response.getItems()) {

                    Permit permit =
                            Permit.builder()
                                    .property(property)
                                    .permitNumber(
                                            permitData.getNumber()
                                    )
                                    .permitType(
                                            permitData.getType()
                                    )
                                    .status(
                                            permitData.getStatus()
                                    )
                                    .issuedDate(
                                            parseDate(
                                                    permitData.getIssue_date()
                                            )
                                    )
                                    .description(
                                            permitData.getDescription()
                                    )
                                    .build();

                    permits.add(permit);
                }
            }

            // -------------------------------------------------
            // 9. GET NEXT CURSOR
            // -------------------------------------------------

            cursor = response.getNext_cursor();

        } while (cursor != null &&
                !cursor.isBlank());

        // -----------------------------------------------------
        // 10. NO PERMITS FOUND
        // -----------------------------------------------------

        if (permits.isEmpty()) {
            return permits;
        }

        // -----------------------------------------------------
        // 11. SAVE ALL PERMITS
        // -----------------------------------------------------

        return permitRepository.saveAll(permits);
    }

    // =========================================================
    // DATE CONVERSION
    // =========================================================

    private LocalDate parseDate(String date) {

        if (date == null ||
                date.isBlank()) {

            return null;
        }

        try {

            return LocalDate.parse(date);

        } catch (Exception exception) {

            return null;
        }
    }
}