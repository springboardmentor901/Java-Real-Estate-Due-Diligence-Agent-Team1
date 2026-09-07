package com.realestate.due_diligence_agent.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.dto.ShovelsPermitResponse;
import com.realestate.due_diligence_agent.exception.ExternalApiException;

import tools.jackson.databind.JsonNode;

@Service
public class ShovelsService {

    private final WebClient webClient;

    @Value("${SHOVELS_API_KEY}")
    private String apiKey;

    public ShovelsService(WebClient.Builder webClientBuilder) {

        this.webClient = webClientBuilder
                .baseUrl("https://api.shovels.ai/v2")
                .build();
    }


    // =========================================================
    // 1. Address Search
    // =========================================================

    public JsonNode searchAddress(String address) {

        try {

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/addresses/search")
                            .queryParam("q", address)
                            .build())
                    .header("X-API-Key", apiKey)
                    .header("Accept", "application/json")
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> response.createException()
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

        } catch (Exception exception) {

            throw new ExternalApiException(
                    "Shovels address search service is currently unavailable",
                    exception
            );
        }
    }


    // =========================================================
    // 2. Permit Search
    // =========================================================
        
    public ShovelsPermitResponse searchPermits(
        String geoId,
        String permitFrom,
        String permitTo,
        String propertyType,
        Integer size,
        String cursor) {

    try {

        return webClient.get()
                .uri(uriBuilder -> {

                    uriBuilder
                            .path("/permits/search")
                            .queryParam("geo_id", geoId);

                    if (permitFrom != null &&
                            !permitFrom.isBlank()) {

                        uriBuilder.queryParam(
                                "permit_from",
                                permitFrom
                        );
                    }

                    if (permitTo != null &&
                            !permitTo.isBlank()) {

                        uriBuilder.queryParam(
                                "permit_to",
                                permitTo
                        );
                    }

                    if (propertyType != null &&
                            !propertyType.isBlank()) {

                        uriBuilder.queryParam(
                                "property_type",
                                propertyType
                        );
                    }

                    if (size != null) {

                        uriBuilder.queryParam(
                                "size",
                                size
                        );
                    }

                    if (cursor != null &&
                            !cursor.isBlank()) {

                        uriBuilder.queryParam(
                                "cursor",
                                cursor
                        );
                    }

                    return uriBuilder.build();
                })
                .header("X-API-Key", apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.createException()
                )
                .bodyToMono(ShovelsPermitResponse.class)
                .block();

    } catch (Exception exception) {

        exception.printStackTrace();

        throw new ExternalApiException(
                "Shovels permit search service is currently unavailable",
                exception
        );
    }
}
}