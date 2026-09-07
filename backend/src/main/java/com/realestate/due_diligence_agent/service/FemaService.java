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

    public FemaService(
            WebClient.Builder webClientBuilder,
            @Value("${FEMA_NFHL_BASE_URL}") String baseUrl) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    // =========================================================
    // FEMA Flood Hazard Zone
    // Layer 28
    // =========================================================

   public JsonNode getFloodZone(
        double latitude,
        double longitude) {

    try {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/20/query")
                        .queryParam("where", "1=1")
                        .queryParam(
                                "geometry",
                                longitude + "," + latitude
                        )
                        .queryParam(
                                "geometryType",
                                "esriGeometryPoint"
                        )
                        .queryParam(
                                "inSR",
                                "4326"
                        )
                        .queryParam(
                                "spatialRel",
                                "esriSpatialRelIntersects"
                        )
                        .queryParam(
                                "outFields",
                                "FLD_ZONE,ZONE_SUBTY,SFHA_TF,STATIC_BFE"
                        )
                        .queryParam(
                                "returnGeometry",
                                "false"
                        )
                        .queryParam(
                                "f",
                                "json"
                        )
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

    } catch (Exception exception) {

    exception.printStackTrace();

    throw new ExternalApiException(
        "FEMA ERROR: " + exception.getClass().getSimpleName()
                + " - " + exception.getMessage(),
        exception
    );
}
}

    // =========================================================
    // FEMA FIRM Panel
    // Layer 3
    // =========================================================

    public JsonNode getFirmPanel(
            double latitude,
            double longitude) {

        try {

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/1/query")

                            .queryParam("where", "1=1")

                            .queryParam(
                                    "geometry",
                                    longitude + "," + latitude
                            )

                            .queryParam(
                                    "geometryType",
                                    "esriGeometryPoint"
                            )

                            .queryParam(
                                    "inSR",
                                    "4326"
                            )

                            .queryParam(
                                    "spatialRel",
                                    "esriSpatialRelIntersects"
                            )

                            .queryParam(
                                    "outFields",
                                    "*"
                            )

                            .queryParam(
                                    "returnGeometry",
                                    "false"
                            )

                            .queryParam(
                                    "f",
                                    "json"
                            )

                            .build())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            errorResponse ->
                                    errorResponse.createException()
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

        } catch (Exception exception) {

            exception.printStackTrace();

throw new ExternalApiException(
        "FEMA ERROR: " + exception.getMessage(),
        exception
);
        }
    }
}