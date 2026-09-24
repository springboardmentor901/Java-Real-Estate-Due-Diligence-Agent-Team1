package com.realestate.due_diligence_agent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.exception.ExternalApiException;

import tools.jackson.databind.JsonNode;

@Service
public class FemaService {

    private final WebClient webClient;
    private final WebClient fallbackWebClient;

    public FemaService(
            WebClient.Builder webClientBuilder,
            @Value("${FEMA_NFHL_BASE_URL}") String baseUrl) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.fallbackWebClient = webClientBuilder
                .baseUrl("https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/USA_Flood_Hazard_Reduced_Set_gdb/FeatureServer")
                .build();
    }

    public boolean isWithinUs(double latitude, double longitude) {
        return latitude >= 18.0 && latitude <= 72.0 && longitude >= -175.0 && longitude <= -65.0;
    }

    // =========================================================
    // FEMA Flood Hazard Zone
    // =========================================================

    public JsonNode getFloodZone(
            double latitude,
            double longitude) {

        if (!isWithinUs(latitude, longitude)) {
            throw new ExternalApiException("FEMA National Flood Hazard Layer is only available for properties within the United States.");
        }

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/20/query")
                            .queryParam("where", "1=1")
                            .queryParam("geometry", longitude + "," + latitude)
                            .queryParam("geometryType", "esriGeometryPoint")
                            .queryParam("inSR", "4326")
                            .queryParam("spatialRel", "esriSpatialRelIntersects")
                            .queryParam("outFields", "FLD_ZONE,ZONE_SUBTY,SFHA_TF,STATIC_BFE")
                            .queryParam("returnGeometry", "false")
                            .queryParam("f", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

        } catch (Exception primaryEx) {
            System.err.println("Primary FEMA endpoint failed (" + primaryEx.getMessage() + "). Trying official Esri NFHL service...");
            try {
                return fallbackWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/0/query")
                                .queryParam("where", "1=1")
                                .queryParam("geometry", longitude + "," + latitude)
                                .queryParam("geometryType", "esriGeometryPoint")
                                .queryParam("inSR", "4326")
                                .queryParam("spatialRel", "esriSpatialRelIntersects")
                                .queryParam("outFields", "FLD_ZONE,ZONE_SUBTY,SFHA_TF,STATIC_BFE,DFIRM_ID")
                                .queryParam("returnGeometry", "false")
                                .queryParam("f", "json")
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();
            } catch (Exception fallbackEx) {
                System.err.println("Esri NFHL fallback also failed: " + fallbackEx.getMessage());
                throw new ExternalApiException(
                        "FEMA service is currently unreachable: " + fallbackEx.getMessage(),
                        fallbackEx
                );
            }
        }
    }

    // =========================================================
    // FEMA FIRM Panel
    // =========================================================

    public JsonNode getFirmPanel(
            double latitude,
            double longitude) {

        if (!isWithinUs(latitude, longitude)) {
            return null;
        }

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/1/query")
                            .queryParam("where", "1=1")
                            .queryParam("geometry", longitude + "," + latitude)
                            .queryParam("geometryType", "esriGeometryPoint")
                            .queryParam("inSR", "4326")
                            .queryParam("spatialRel", "esriSpatialRelIntersects")
                            .queryParam("outFields", "*")
                            .queryParam("returnGeometry", "false")
                            .queryParam("f", "json")
                            .build())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            errorResponse -> errorResponse.createException()
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

        } catch (Exception exception) {
            // Optional panel data - do not fail if unavailable
            return null;
        }
    }
}