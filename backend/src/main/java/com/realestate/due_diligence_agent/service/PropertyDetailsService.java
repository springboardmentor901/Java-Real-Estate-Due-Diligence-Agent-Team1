package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class PropertyDetailsService {

    private final PropertyRepository propertyRepository;
    private final AttomService attomService;

    public Property getPropertyDetails(Long id) {

        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + id
                        ));
    }

    @Transactional
    public Property fetchAndSaveBasicProfile(Long id) {

        // 1. Get property from database
        Property property = propertyRepository.findById(id)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + id
                        ));

        // 2. Call ATTOM Basic Profile
        JsonNode response =
                attomService.getBasicProfile(property.getAddress());

        // 3. Validate ATTOM response
        if (response == null ||
                response.get("property") == null ||
                !response.get("property").isArray() ||
                response.get("property").isEmpty()) {

            throw new RuntimeException(
                    "No property data found from ATTOM for address: "
                            + property.getAddress()
            );
        }

        JsonNode attomProperty =
                response.get("property").get(0);

        // 4. Property summary
        JsonNode summary =
                attomProperty.get("summary");

        if (summary != null) {

            if (summary.get("propertyType") != null) {
                property.setPropertyType(
                        summary.get("propertyType").asText()
                );
            }

            if (summary.get("yearBuilt") != null) {
                property.setYearBuilt(
                        summary.get("yearBuilt").asInt()
                );
            }
        }

        // 5. Building information
        JsonNode building =
                attomProperty.get("building");

        if (building != null) {

            JsonNode rooms =
                    building.get("rooms");

            if (rooms != null) {

                if (rooms.get("beds") != null) {
                    property.setBedrooms(
                            rooms.get("beds").asInt()
                    );
                }

                if (rooms.get("bathsTotal") != null) {
                    property.setBathrooms(
                            rooms.get("bathsTotal").asDouble()
                    );
                }
            }

            JsonNode size =
                    building.get("size");

            if (size != null &&
                    size.get("livingSize") != null) {

                property.setSquareFeet(
                        size.get("livingSize").asDouble()
                );
            }
        }

        // 6. Location
        JsonNode location =
                attomProperty.get("location");

        if (location != null) {

            if (location.get("latitude") != null) {
                property.setLatitude(
                        location.get("latitude").asDouble()
                );
            }

            if (location.get("longitude") != null) {
                property.setLongitude(
                        location.get("longitude").asDouble()
                );
            }
        }

        // 7. Save updated property
        return propertyRepository.save(property);
    }
}