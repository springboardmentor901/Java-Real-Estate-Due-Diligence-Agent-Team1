package com.realestate.due_diligence_agent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.dto.RegridResponse;
import com.realestate.due_diligence_agent.exception.ExternalApiException;

@Service
public class RegridService {

    private final WebClient webClient;

    @Value("${regrid.api-token}")
    private String apiToken;

    public RegridService(WebClient.Builder webClientBuilder) {

        this.webClient = webClientBuilder
                .baseUrl("https://app.regrid.com/api/v2")
                .build();
    }

    public RegridResponse getParcelData(
            double latitude,
            double longitude) {

        try {

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/parcels/point")

                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)

                            // Search within 250 meters
                            .queryParam("radius", 250)

                            // Return only one parcel
                            .queryParam("limit", 1)

                            // Request zoning information
                            .queryParam("return_zoning", true)

                            // Request field labels
                            .queryParam("return_field_labels", true)

                            // Regrid API token
                            .queryParam("token", apiToken)

                            .build())
                    .retrieve()
                    .bodyToMono(RegridResponse.class)
                    .block();

        } catch (Exception exception) {

            throw new ExternalApiException(
                    "REGRID service is currently unavailable",
                    exception
            );
        }
    }
}