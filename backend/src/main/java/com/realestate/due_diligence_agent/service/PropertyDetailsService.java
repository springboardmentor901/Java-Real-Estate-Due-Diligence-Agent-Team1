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

        Property property = propertyRepository.findById(id)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + id
                        ));

        if (property.getBedrooms() == null && property.getSquareFeet() == null && property.getYearBuilt() == null && property.getPropertyType() == null) {
            try {
                Property enriched = enrichPropertyFromExternal(property);
                if (enriched.getBedrooms() != null || enriched.getPropertyType() != null || enriched.getSquareFeet() != null) {
                    property = propertyRepository.save(enriched);
                }
            } catch (Exception ignored) {}
        }

        return property;
    }

    public Property enrichPropertyFromExternal(Property property) {
        if (property == null || property.getAddress() == null || property.getAddress().isBlank()) {
            return property;
        }

        try {
            JsonNode response = attomService.getBasicProfile(property.getAddress());

            if (response != null &&
                    response.get("property") != null &&
                    response.get("property").isArray() &&
                    !response.get("property").isEmpty()) {

                JsonNode attomProperty = response.get("property").get(0);

                // Summary
                JsonNode summary = attomProperty.get("summary");
                if (summary != null) {
                    if (summary.get("propertyType") != null && !summary.get("propertyType").asText().isBlank()) {
                        property.setPropertyType(summary.get("propertyType").asText());
                    }
                    if (summary.get("yearBuilt") != null) {
                        property.setYearBuilt(summary.get("yearBuilt").asInt());
                    }
                }

                // Building
                JsonNode building = attomProperty.get("building");
                if (building != null) {
                    JsonNode rooms = building.get("rooms");
                    if (rooms != null) {
                        if (rooms.get("beds") != null) {
                            property.setBedrooms(rooms.get("beds").asInt());
                        }
                        if (rooms.get("bathsTotal") != null) {
                            property.setBathrooms(rooms.get("bathsTotal").asDouble());
                        }
                    }

                    JsonNode size = building.get("size");
                    if (size != null && size.get("livingSize") != null) {
                        property.setSquareFeet(size.get("livingSize").asDouble());
                    }
                }

                // Location
                JsonNode location = attomProperty.get("location");
                if (location != null) {
                    if (location.get("latitude") != null && (property.getLatitude() == null || property.getLatitude() == 0.0)) {
                        property.setLatitude(location.get("latitude").asDouble());
                    }
                    if (location.get("longitude") != null && (property.getLongitude() == null || property.getLongitude() == 0.0)) {
                        property.setLongitude(location.get("longitude").asDouble());
                    }
                }
            }
        } catch (Exception exception) {
            // External property aggregator unavailable or address not found/unsupported (e.g. international)
            // Fields remain null so application correctly displays N/A without failing
        }

        return property;
    }

    @Transactional
    public Property fetchAndSaveBasicProfile(Long id) {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: " + id
                        ));

        enrichPropertyFromExternal(property);
        return propertyRepository.save(property);
    }
}