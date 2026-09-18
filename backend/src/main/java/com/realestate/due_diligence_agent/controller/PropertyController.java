package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.PropertyRequest;
import com.realestate.due_diligence_agent.dto.ShovelsPermitResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.service.GeocodingService;
import com.realestate.due_diligence_agent.service.PropertyDetailsService;
import com.realestate.due_diligence_agent.service.PropertyService;
import com.realestate.due_diligence_agent.service.ShovelsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final GeocodingService geocodingService;
    private final PropertyDetailsService propertyDetailsService;
    private final ShovelsService shovelsService;


    // =========================================================
    // CREATE PROPERTY
    // =========================================================

    @PostMapping
    public Property createProperty(
            @Valid @RequestBody PropertyRequest request) {

        GeocodingService.GeocodingResult location =
                geocodingService.geocode(request.getAddress());

        Property property = Property.builder()
                .address(request.getAddress())
                .propertyType(request.getPropertyType())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .squareFeet(request.getSquareFeet())
                .yearBuilt(request.getYearBuilt())
                .latitude(Double.parseDouble(location.getLat()))
                .longitude(Double.parseDouble(location.getLon()))
                .build();

        return propertyService.saveProperty(property);
    }


    // =========================================================
    // GET ALL PROPERTIES
    // =========================================================

    @GetMapping
    public List<Property> getAllProperties() {

        return propertyService.getAllProperties();
    }


    // =========================================================
    // SEARCH PROPERTIES
    // =========================================================

    @GetMapping("/search")
    public List<Property> searchProperties(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "query", required = false) String query) {

        String searchTerm = (address != null && !address.isBlank()) ? address : query;

        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        return propertyService.searchProperties(searchTerm.trim());
    }


    // =========================================================
    // GET PROPERTY BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Property getPropertyById(
            @PathVariable Long id) {

        return propertyService.getPropertyById(id);
    }


    // =========================================================
    // GEOCODE ADDRESS
    // =========================================================

    @GetMapping("/geocode")
    public GeocodingService.GeocodingResult geocodeAddress(
            @RequestParam String address) {

        return geocodingService.geocode(address);
    }


    // =========================================================
    // GET PROPERTY DETAILS
    // =========================================================

    @GetMapping("/{id}/details")
    public Property getPropertyDetails(
            @PathVariable Long id) {

        return propertyDetailsService.getPropertyDetails(id);
    }


    // =========================================================
    // FETCH ATTOM BASIC PROFILE
    // =========================================================

    @GetMapping("/{id}/basic-profile")
    public Property fetchBasicProfile(
            @PathVariable Long id) {

        return propertyDetailsService.fetchAndSaveBasicProfile(id);
    }


    // =========================================================
    // TEMPORARY SHOVELS ADDRESS SEARCH
    //
    // Used to convert an address into a Shovels geo_id.
    // Example:
    //
    // GET /api/properties/shovels/address-search?address=...
    // =========================================================

    @GetMapping("/shovels/address-search")
    public tools.jackson.databind.JsonNode searchShovelsAddress(
            @RequestParam String address) {

        return shovelsService.searchAddress(address);
    }


    // =========================================================
    // TEMPORARY SHOVELS PERMIT SEARCH
    //
    // Used for testing the Shovels API directly.
    //
    // Example:
    //
    // GET /api/properties/shovels/permits-search
    //      ?geoId=DiT08iX46JE
    //      &permitFrom=2020-01-01
    //      &permitTo=2024-12-31
    // =========================================================

    @GetMapping("/shovels/permits-search")
    public ShovelsPermitResponse searchShovelsPermits(
            @RequestParam String geoId,
            @RequestParam(required = false) String permitFrom,
            @RequestParam(required = false) String permitTo,
            @RequestParam(required = false) String propertyType) {

        return shovelsService.searchPermits(
                geoId,
                permitFrom,
                permitTo,
                propertyType,
                100,
                null
        );
    }
}