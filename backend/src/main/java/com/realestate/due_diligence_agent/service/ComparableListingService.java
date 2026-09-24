package com.realestate.due_diligence_agent.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.client.RapidApiListingClient;
import com.realestate.due_diligence_agent.dto.RapidApiListingResponse;
import com.realestate.due_diligence_agent.entity.ComparableListing;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.ComparableListingRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.util.AddressParser;
import com.realestate.due_diligence_agent.util.DistanceCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComparableListingService {

    private final ComparableListingRepository comparableListingRepository;
    private final PropertyRepository propertyRepository;
    private final RapidApiListingClient rapidApiListingClient;

    public List<ComparableListing> getComparables(Long propertyId) {

        // Step 1: Find the property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found with id: " + propertyId));

        // Step 2: Check database first
        List<ComparableListing> savedComparables =
                comparableListingRepository.findByPropertyId(propertyId);

        // If comparables already exist, return them
        if (!savedComparables.isEmpty()) {
            return new ArrayList<>(savedComparables);
        }

        // Step 3: No saved comparables, so call RapidAPI
        String location = buildSearchLocation(property.getAddress());
        if (location == null || location.isBlank()) {
            return new ArrayList<>();
        }

        tools.jackson.databind.JsonNode response = null;
        try {
            response = rapidApiListingClient.searchListings(location);
        } catch (Exception e) {
            System.err.println("RapidAPI listing search failed for location '" + location + "': " + e.getMessage());
            return new ArrayList<>();
        }

        if (response == null || !response.hasNonNull("data") || !response.get("data").hasNonNull("results")) {
            return new ArrayList<>();
        }

        tools.jackson.databind.JsonNode resultsNode = response.get("data").get("results");
        if (!resultsNode.isArray() || resultsNode.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 5: Convert API listings into ComparableListing entities
        List<ComparableListing> comparables = new ArrayList<>();

        Double propertyLat = property.getLatitude();
        Double propertyLon = property.getLongitude();

        for (tools.jackson.databind.JsonNode listing : resultsNode) {
            if (listing == null || listing.isNull()) {
                continue;
            }

            // Address
            String comparableAddress = buildAddressFromJson(listing);

            // Price
            Double price = null;
            if (listing.hasNonNull("list_price")) {
                price = listing.get("list_price").asDouble();
            } else if (listing.hasNonNull("last_sold_price")) {
                price = listing.get("last_sold_price").asDouble();
            }

            // Square feet
            Double squareFeet = null;
            tools.jackson.databind.JsonNode desc = listing.get("description");
            if (desc != null && desc.hasNonNull("sqft")) {
                squareFeet = desc.get("sqft").asDouble();
            }

            // Listed date
            LocalDateTime listedDate = null;
            if (listing.hasNonNull("list_date")) {
                String dateStr = listing.get("list_date").asText();
                try {
                    listedDate = LocalDateTime.ofInstant(
                            java.time.Instant.parse(dateStr),
                            java.time.ZoneId.systemDefault()
                    );
                } catch (Exception ignored) {
                    try {
                        listedDate = LocalDateTime.parse(dateStr);
                    } catch (Exception ignored2) {}
                }
            }

            // Source
            String source = null;
            tools.jackson.databind.JsonNode sourceNode = listing.get("source");
            if (sourceNode != null && sourceNode.hasNonNull("name")) {
                source = sourceNode.get("name").asText();
            }

            // Coordinates
            Double comparableLat = null;
            Double comparableLon = null;
            tools.jackson.databind.JsonNode coordNode = listing.path("location").path("address").path("coordinate");
            if (coordNode != null && coordNode.hasNonNull("lat") && coordNode.hasNonNull("lon")) {
                comparableLat = coordNode.get("lat").asDouble();
                comparableLon = coordNode.get("lon").asDouble();
            }

            // Calculate distance
            Double distanceMiles = null;
            if (propertyLat != null && propertyLon != null
                    && comparableLat != null && comparableLon != null) {
                distanceMiles = DistanceCalculator.calculateMiles(
                        propertyLat, propertyLon, comparableLat, comparableLon
                );
            }

            // Create entity
            ComparableListing comparableListing = ComparableListing.builder()
                    .comparableAddress(comparableAddress)
                    .price(price)
                    .squareFeet(squareFeet)
                    .distanceMiles(distanceMiles)
                    .listedDate(listedDate)
                    .source(source)
                    .property(property)
                    .build();

            comparables.add(comparableListing);
        }

        // Step 6: Save all comparables
        if (comparables.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                comparableListingRepository.saveAll(comparables)
        );
    }

    private String buildAddressFromJson(tools.jackson.databind.JsonNode listing) {
        tools.jackson.databind.JsonNode addrNode = listing.path("location").path("address");
        if (addrNode == null || addrNode.isMissingNode()) {
            return "Address unavailable";
        }

        String line = addrNode.hasNonNull("line") ? addrNode.get("line").asText().trim() : "";
        String city = addrNode.hasNonNull("city") ? addrNode.get("city").asText().trim() : "";
        String state = addrNode.hasNonNull("state_code") ? addrNode.get("state_code").asText().trim() : "";
        String postal = addrNode.hasNonNull("postal_code") ? addrNode.get("postal_code").asText().trim() : "";

        StringBuilder result = new StringBuilder();
        if (!line.isEmpty()) {
            result.append(line);
        }
        if (!city.isEmpty()) {
            if (result.length() > 0) result.append(", ");
            result.append(city);
        }
        if (!state.isEmpty()) {
            if (result.length() > 0) result.append(", ");
            result.append(state);
        }
        if (!postal.isEmpty()) {
            if (result.length() > 0) result.append(" ");
            result.append(postal);
        }

        return result.length() > 0 ? result.toString() : "Address unavailable";
    }

    private String buildSearchLocation(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        AddressParser parser = AddressParser.parse(address);
        if (parser.isUsAddress()) {
            if (!parser.getCity().isBlank() && !parser.getState().isBlank()) {
                return parser.getCity() + ", " + parser.getState();
            }
            if (!parser.getZip().isBlank()) {
                return parser.getZip();
            }
        }
        return address;
    }
}