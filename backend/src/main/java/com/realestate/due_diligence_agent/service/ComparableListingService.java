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

        RapidApiListingResponse response = null;
        try {
            response = rapidApiListingClient.searchListings(location);
        } catch (Exception e) {
            System.err.println("RapidAPI listing search failed for location '" + location + "': " + e.getMessage());
            return new ArrayList<>();
        }

        if (response == null) {
            return new ArrayList<>();
        }

        // Step 4: Read the actual API structure:
        // response -> data -> results
        List<RapidApiListingResponse.Listing> listings =
                response.getData() != null
                        && response.getData().getResults() != null
                        ? response.getData().getResults()
                        : List.of();

        if (listings.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 5: Convert API listings into ComparableListing entities
        List<ComparableListing> comparables = new ArrayList<>();

        for (RapidApiListingResponse.Listing listing : listings) {

            if (listing == null) {
                continue;
            }

            // Address
            String comparableAddress = buildAddress(listing);

            // Price
            Double price = listing.getListPrice();
            // Square feet
                Double squareFeet = null;

                if (listing.getDescription() != null) {
                        squareFeet = listing.getDescription().getSqft().doubleValue();
                }

            // Listed date
            LocalDateTime listedDate = null;

            if (listing.getListDate() != null) {
                listedDate = LocalDateTime.ofInstant(
                        listing.getListDate(),
                        java.time.ZoneId.systemDefault()
                );
            }

            // Source
            String source = null;

            if (listing.getSource() != null) {
                source = listing.getSource().getName();
            }

            // Coordinates
            Double comparableLat = null;
            Double comparableLon = null;

            if (listing.getLocation() != null
                    && listing.getLocation().getAddress() != null
                    && listing.getLocation().getAddress().getCoordinate() != null) {

                comparableLat =
                        listing.getLocation()
                                .getAddress()
                                .getCoordinate()
                                .getLat();

                comparableLon =
                        listing.getLocation()
                                .getAddress()
                                .getCoordinate()
                                .getLon();
            }

            // Property coordinates
            Double propertyLat = null;
            Double propertyLon = null;

            /*
             * This section assumes your Property entity contains
             * latitude and longitude fields.
             *
             * If your Property entity uses different field names,
             * we will adjust this after compilation.
             */
            try {
                propertyLat = property.getLatitude();
                propertyLon = property.getLongitude();
            } catch (Exception ignored) {
                // Leave distance as null if property coordinates
                // are not available.
            }

            // Calculate distance
            Double distanceMiles = null;

            if (propertyLat != null
                    && propertyLon != null
                    && comparableLat != null
                    && comparableLon != null) {

                distanceMiles = DistanceCalculator.calculateMiles(
                        propertyLat,
                        propertyLon,
                        comparableLat,
                        comparableLon
                );
            }

            // Create entity
            ComparableListing comparableListing =
                    ComparableListing.builder()
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

    private String buildAddress(
            RapidApiListingResponse.Listing listing) {

        if (listing.getLocation() == null
                || listing.getLocation().getAddress() == null) {
            return "Address unavailable";
        }

        RapidApiListingResponse.Address address =
                listing.getLocation().getAddress();

        StringBuilder result = new StringBuilder();

        if (address.getLine() != null
                && !address.getLine().isBlank()) {
            result.append(address.getLine());
        }

        if (address.getCity() != null
                && !address.getCity().isBlank()) {

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(address.getCity());
        }

        if (address.getStateCode() != null
                && !address.getStateCode().isBlank()) {

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(address.getStateCode());
        }

        if (address.getPostalCode() != null
                && !address.getPostalCode().isBlank()) {

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(address.getPostalCode());
        }

        if (result.length() == 0) {
            return "Address unavailable";
        }

        return result.toString();
    }
    private String buildSearchLocation(String address) {
        if (address == null || address.isBlank()) {
            throw new RuntimeException("Property address is missing");
        }

        String[] parts = address.split(",");

        // Check if address ends with "United States" or "USA"
        if (parts.length >= 2 && parts[parts.length - 1].trim().equalsIgnoreCase("United States")) {
            String city = parts[0].trim();
            String stateCandidate = parts[parts.length - 2].trim();
            String stateCode = normalizeUsState(stateCandidate);
            if (stateCode != null) {
                return "city:" + city + ", " + stateCode;
            }
            return "city:" + city + ", " + stateCandidate;
        }

        if (parts.length >= 3) {
            String city = parts[parts.length - 2].trim();
            String stateZip = parts[parts.length - 1].trim();

            String state = stateZip.split("\\s+")[0].trim();

            return "city:" + city + ", " + state;
        }

        return address.trim();
    }

    private static String normalizeUsState(String stateName) {
        if (stateName == null) return null;
        String s = stateName.trim();
        if (s.length() == 2) return s.toUpperCase();
        return switch (s.toLowerCase()) {
            case "alabama" -> "AL";
            case "alaska" -> "AK";
            case "arizona" -> "AZ";
            case "arkansas" -> "AR";
            case "california" -> "CA";
            case "colorado" -> "CO";
            case "connecticut" -> "CT";
            case "delaware" -> "DE";
            case "florida" -> "FL";
            case "georgia" -> "GA";
            case "hawaii" -> "HI";
            case "idaho" -> "ID";
            case "illinois" -> "IL";
            case "indiana" -> "IN";
            case "iowa" -> "IA";
            case "kansas" -> "KS";
            case "kentucky" -> "KY";
            case "louisiana" -> "LA";
            case "maine" -> "ME";
            case "maryland" -> "MD";
            case "massachusetts" -> "MA";
            case "michigan" -> "MI";
            case "minnesota" -> "MN";
            case "mississippi" -> "MS";
            case "missouri" -> "MO";
            case "montana" -> "MT";
            case "nebraska" -> "NE";
            case "nevada" -> "NV";
            case "new hampshire" -> "NH";
            case "new jersey" -> "NJ";
            case "new mexico" -> "NM";
            case "new york" -> "NY";
            case "north carolina" -> "NC";
            case "north dakota" -> "ND";
            case "ohio" -> "OH";
            case "oklahoma" -> "OK";
            case "oregon" -> "OR";
            case "pennsylvania" -> "PA";
            case "rhode island" -> "RI";
            case "south carolina" -> "SC";
            case "south dakota" -> "SD";
            case "tennessee" -> "TN";
            case "texas" -> "TX";
            case "utah" -> "UT";
            case "vermont" -> "VT";
            case "virginia" -> "VA";
            case "washington" -> "WA";
            case "west virginia" -> "WV";
            case "wisconsin" -> "WI";
            case "wyoming" -> "WY";
            case "district of columbia" -> "DC";
            default -> null;
        };
    }
}