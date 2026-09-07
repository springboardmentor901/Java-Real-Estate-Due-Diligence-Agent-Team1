package com.realestate.due_diligence_agent.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.exception.ExternalApiException;

import tools.jackson.databind.JsonNode;

@Service
public class ElevationService {

    private final WebClient webClient;

    public ElevationService(WebClient.Builder webClientBuilder) {

        this.webClient = webClientBuilder
                .baseUrl("https://epqs.nationalmap.gov")
                .build();
    }

    public Double getElevation(
            double latitude,
            double longitude) {

        try {

            JsonNode elevationResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/json")
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("wkid", 4326)
                            .queryParam("units", "Meters")
                            .queryParam("includeDate", "false")
                            .build())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            errorResponse -> errorResponse.createException()
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

            if (elevationResponse == null ||
                    elevationResponse.get("value") == null ||
                    elevationResponse.get("value").isNull()) {

                throw new RuntimeException(
                        "No elevation data returned from USGS"
                );
            }

            return elevationResponse
                    .get("value")
                    .asDouble();

        } catch (Exception exception) {

            throw new ExternalApiException(
                    "USGS elevation service is currently unavailable",
                    exception
            );
        }
    }
}