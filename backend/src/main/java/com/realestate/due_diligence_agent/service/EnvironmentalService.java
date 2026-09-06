package com.realestate.due_diligence_agent.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.EnvironmentalRecord;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.EnvironmentalRecordRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

import tools.jackson.databind.JsonNode;

@Service
public class EnvironmentalService {

    private final EnvironmentalRecordRepository environmentalRecordRepository;
    private final PropertyRepository propertyRepository;
    private final EPAEnvirofactsService epaService;

    public EnvironmentalService(
            EnvironmentalRecordRepository environmentalRecordRepository,
            PropertyRepository propertyRepository,
            EPAEnvirofactsService epaService) {

        this.environmentalRecordRepository =
                environmentalRecordRepository;

        this.propertyRepository =
                propertyRepository;

        this.epaService =
                epaService;
    }

    // =========================================================
    // GET ENVIRONMENTAL RECORDS
    // =========================================================

    @Transactional
    public List<EnvironmentalRecord> getEnvironmentalRecords(
            Long propertyId) {

        // -----------------------------------------------------
        // 1. DATABASE-FIRST CHECK
        // -----------------------------------------------------

        List<EnvironmentalRecord> existingRecords =
                environmentalRecordRepository
                        .findByPropertyId(propertyId);

        if (!existingRecords.isEmpty()) {

            System.out.println(
                    "Environmental records found in database: "
                            + existingRecords.size()
            );

            return existingRecords;
        }

        System.out.println(
                "No environmental records found in database. "
                        + "Calling EPA..."
        );


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
        // 4. PARSE ADDRESS
        // -----------------------------------------------------

        AddressParts addressParts =
                parseAddress(property.getAddress());

        System.out.println(
                "EPA search address: "
                        + addressParts.streetAddress
                        + ", "
                        + addressParts.city
                        + ", "
                        + addressParts.state
                        + " "
                        + addressParts.zipCode
        );


        // -----------------------------------------------------
        // 5. SEARCH EPA FRS BY EXACT ADDRESS
        // -----------------------------------------------------

        JsonNode response =
                epaService.searchFacilities(
                        addressParts.streetAddress,
                        addressParts.city,
                        addressParts.state,
                        addressParts.zipCode,
                        null
                );


        // -----------------------------------------------------
        // DEBUG EPA ADDRESS RESPONSE
        // -----------------------------------------------------

        System.out.println(
                "========== EPA ADDRESS RESPONSE =========="
        );

        System.out.println(response);

        System.out.println(
                "=========================================="
        );


        // -----------------------------------------------------
        // 6. MAP ADDRESS RESULTS
        // -----------------------------------------------------

        List<EnvironmentalRecord> records =
                new ArrayList<>();

        mapResponse(
                response,
                property,
                records
        );


        System.out.println(
                "Environmental records mapped from "
                        + "address search: "
                        + records.size()
        );


        // -----------------------------------------------------
        // 7. FALLBACK TO COORDINATE SEARCH
        // -----------------------------------------------------

        if (records.isEmpty() &&
                property.getLatitude() != null &&
                property.getLongitude() != null) {

            System.out.println(
                    "No exact-address environmental records "
                            + "found. Trying coordinate search..."
            );


            JsonNode coordinateResponse =
                    epaService.searchFacilitiesByCoordinates(
                            property.getLatitude(),
                            property.getLongitude(),
                            0.1,
                            null
                    );


            // -------------------------------------------------
            // DEBUG COORDINATE RESPONSE
            // -------------------------------------------------

            System.out.println(
                    "========== EPA COORDINATE RESPONSE =========="
            );

            System.out.println(coordinateResponse);

            System.out.println(
                    "=============================================="
            );


            // -------------------------------------------------
            // MAP COORDINATE RESULTS
            // -------------------------------------------------

            mapResponse(
                    coordinateResponse,
                    property,
                    records
            );


            System.out.println(
                    "Environmental records mapped from "
                            + "coordinate search: "
                            + records.size()
            );
        }


        // -----------------------------------------------------
        // 8. NOTHING FOUND
        // -----------------------------------------------------

        if (records.isEmpty()) {

            System.out.println(
                    "No relevant environmental records "
                            + "found from EPA."
            );

            return records;
        }


        // -----------------------------------------------------
        // 9. SET RETRIEVED TIME
        // -----------------------------------------------------

        LocalDateTime now =
                LocalDateTime.now();

        for (EnvironmentalRecord record : records) {

            record.setRetrievedAt(now);
        }


        // -----------------------------------------------------
        // 10. SAVE TO DATABASE
        // -----------------------------------------------------

        List<EnvironmentalRecord> savedRecords =
                environmentalRecordRepository
                        .saveAll(records);


        System.out.println(
                "Environmental records saved: "
                        + savedRecords.size()
        );


        return savedRecords;
    }


    // =========================================================
    // MAP EPA RESPONSE
    // =========================================================

    private void mapResponse(
            JsonNode response,
            Property property,
            List<EnvironmentalRecord> records) {

        if (response == null ||
                response.isNull()) {

            System.out.println(
                    "EPA response is NULL."
            );

            return;
        }


        // -----------------------------------------------------
        // EPA STRUCTURE:
        //
        // Results
        //    └── FRSFacility
        // -----------------------------------------------------

        JsonNode results =
                response.path("Results");

        if (results.isMissingNode() ||
                results.isNull()) {

            System.out.println(
                    "EPA response does not contain Results."
            );

            return;
        }


        JsonNode facilities =
                results.path("FRSFacility");


        if (!facilities.isArray()) {

            System.out.println(
                    "EPA response does not contain "
                            + "FRSFacility array."
            );

            return;
        }


        System.out.println(
                "EPA facilities received: "
                        + facilities.size()
        );


        // -----------------------------------------------------
        // PROCESS EACH FACILITY
        // -----------------------------------------------------

        for (JsonNode facility : facilities) {

            String registryId =
                    textValue(
                            facility,
                            "RegistryId"
                    );

            String facilityName =
                    textValue(
                            facility,
                            "FacilityName"
                    );

            String locationAddress =
                    textValue(
                            facility,
                            "LocationAddress"
                    );

            String city =
                    textValue(
                            facility,
                            "CityName"
                    );

            String state =
                    textValue(
                            facility,
                            "StateAbbr"
                    );

            String zip =
                    textValue(
                            facility,
                            "ZipCode"
                    );


            System.out.println(
                    "EPA Facility: "
                            + facilityName
                            + " | Registry ID: "
                            + registryId
            );


            // -------------------------------------------------
            // PROGRAM FACILITIES
            // -------------------------------------------------

            JsonNode programs =
                    facility.path("ProgramFacilities");


            boolean relevantProgramFound =
                    false;


            if (programs.isArray()) {

                for (JsonNode program : programs) {

                    String acronym =
                            textValue(
                                    program,
                                    "ProgramSystemAcronym"
                            );

                    String programId =
                            textValue(
                                    program,
                                    "ProgramSystemId"
                            );

                    String programFacilityName =
                            textValue(
                                    program,
                                    "ProgramFacilityName"
                            );


                    System.out.println(
                            "EPA Program: "
                                    + acronym
                    );


                    // -----------------------------------------
                    // CHECK RELEVANT PROGRAM
                    // -----------------------------------------

                    if (!isRelevantProgram(acronym)) {

                        continue;
                    }


                    relevantProgramFound =
                            true;


                    // -----------------------------------------
                    // BUILD DESCRIPTION
                    // -----------------------------------------

                    String description =
                            buildDescription(
                                    facilityName,
                                    locationAddress,
                                    city,
                                    state,
                                    zip,
                                    registryId,
                                    acronym,
                                    programId,
                                    programFacilityName
                            );


                    // -----------------------------------------
                    // CREATE ENVIRONMENTAL RECORD
                    // -----------------------------------------

                    EnvironmentalRecord record =
                            EnvironmentalRecord.builder()
                                    .property(property)
                                    .recordType(acronym)
                                    .description(description)
                                    .severity(
                                            determineSeverity(
                                                    acronym
                                            )
                                    )
                                    .source(
                                            "EPA Envirofacts / FRS"
                                    )
                                    .build();


                    records.add(record);
                }
            }


            // -------------------------------------------------
            // NO RELEVANT PROGRAM
            // -------------------------------------------------

            if (!relevantProgramFound) {

                System.out.println(
                        "No relevant environmental program "
                                + "for facility: "
                                + facilityName
                );
            }
        }
    }


    // =========================================================
    // RELEVANT EPA PROGRAMS
    // =========================================================

    private boolean isRelevantProgram(
            String acronym) {

        if (acronym == null ||
                acronym.isBlank()) {

            return false;
        }


        String value =
                acronym.trim().toUpperCase();


        // High-priority environmental programs

        if (value.equals("SEMS") ||
                value.equals("RCRAINFO")) {

            return true;
        }


        // Water discharge/environmental permit

        if (value.equals("NPDES")) {

            return true;
        }


        // Ignore generic FRS registry entries

        if (value.equals("IN-FRS") ||
                value.equals("FRS")) {

            return false;
        }


        // Other EPA program records

        return true;
    }


    // =========================================================
    // DETERMINE SEVERITY
    // =========================================================

    private String determineSeverity(
            String acronym) {

        if (acronym == null) {

            return "INFO";
        }


        String value =
                acronym.trim().toUpperCase();


        if (value.equals("SEMS") ||
                value.equals("RCRAINFO")) {

            return "HIGH";
        }


        if (value.equals("NPDES")) {

            return "MEDIUM";
        }


        return "INFO";
    }


    // =========================================================
    // BUILD DESCRIPTION
    // =========================================================

    private String buildDescription(
            String facilityName,
            String address,
            String city,
            String state,
            String zip,
            String registryId,
            String program,
            String programId,
            String programFacilityName) {

        StringBuilder description =
                new StringBuilder();


        appendValue(
                description,
                "Facility",
                facilityName
        );


        appendValue(
                description,
                "Address",
                address
        );


        appendValue(
                description,
                "City",
                city
        );


        appendValue(
                description,
                "State",
                state
        );


        appendValue(
                description,
                "ZIP",
                zip
        );


        appendValue(
                description,
                "EPA Registry ID",
                registryId
        );


        appendValue(
                description,
                "Program",
                program
        );


        appendValue(
                description,
                "Program ID",
                programId
        );


        appendValue(
                description,
                "Program Facility",
                programFacilityName
        );


        return description.toString();
    }


    // =========================================================
    // APPEND VALUE
    // =========================================================

    private void appendValue(
            StringBuilder builder,
            String label,
            String value) {

        if (value == null ||
                value.isBlank()) {

            return;
        }


        if (builder.length() > 0) {

            builder.append(" | ");
        }


        builder.append(label);
        builder.append(": ");
        builder.append(value);
    }


    // =========================================================
    // GET TEXT VALUE
    // =========================================================

    private String textValue(
            JsonNode node,
            String fieldName) {

        JsonNode value =
                node.get(fieldName);


        if (value == null ||
                value.isNull()) {

            return null;
        }


        String text =
                value.asText();


        if (text == null ||
                text.isBlank()) {

            return null;
        }


        return text;
    }


    // =========================================================
    // PARSE PROPERTY ADDRESS
    // =========================================================

    private AddressParts parseAddress(
            String address) {

        AddressParts result =
                new AddressParts();


        String[] parts =
                address.split(",");


        // -----------------------------------------------------
        // STREET
        // -----------------------------------------------------

        if (parts.length >= 1) {

            result.streetAddress =
                    parts[0].trim();
        }


        // -----------------------------------------------------
        // CITY
        // -----------------------------------------------------

        if (parts.length >= 2) {

            result.city =
                    parts[1].trim();
        }


        // -----------------------------------------------------
        // STATE + ZIP
        // -----------------------------------------------------

        if (parts.length >= 3) {

            String stateZip =
                    parts[2].trim();


            String[] stateZipParts =
                    stateZip.split("\\s+");


            if (stateZipParts.length >= 1) {

                result.state =
                        stateZipParts[0].trim();
            }


            if (stateZipParts.length >= 2) {

                result.zipCode =
                        stateZipParts[1].trim();
            }
        }


        return result;
    }


    // =========================================================
    // ADDRESS PARTS
    // =========================================================

    private static class AddressParts {

        private String streetAddress;

        private String city;

        private String state;

        private String zipCode;
    }
}