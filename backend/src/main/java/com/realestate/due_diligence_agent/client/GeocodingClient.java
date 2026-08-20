package com.realestate.due_diligence_agent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.realestate.due_diligence_agent.dto.AddressValidationResponse;
import com.realestate.due_diligence_agent.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Service
public class GeocodingClient {

    private final WebClient webClient;

    @Value("${geocoding.api.key}")
    private String apiKey;

    public GeocodingClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public AddressValidationResponse validateAddress(String address) {

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address.trim())
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new ExternalApiException(
                        "Empty response from Google Geocoding API",
                        null
                );
            }

            String status = response.path("status").asText();

            // Address does not exist / could not be found
            if ("ZERO_RESULTS".equals(status)) {
                return AddressValidationResponse.builder()
                        .valid(false)
                        .build();
            }

            // Google API returned an error
            if (!"OK".equals(status)) {
                throw new ExternalApiException(
                        "Google Geocoding API failed: " + status,
                        null
                );
            }

            JsonNode result = response
                    .path("results")
                    .get(0);

            if (result == null || result.isMissingNode()) {
                return AddressValidationResponse.builder()
                        .valid(false)
                        .build();
            }

            String formattedAddress =
                    result.path("formatted_address").asText();

            JsonNode location = result
                    .path("geometry")
                    .path("location");

            BigDecimal latitude = BigDecimal.valueOf(
                    location.path("lat").asDouble()
            );

            BigDecimal longitude = BigDecimal.valueOf(
                    location.path("lng").asDouble()
            );

            return AddressValidationResponse.builder()
                    .valid(true)
                    .formattedAddress(formattedAddress)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

        } catch (WebClientResponseException |
                 WebClientRequestException e) {

            throw new ExternalApiException(
                    "Geocoding service call failed", e
            );
        }
    }
}