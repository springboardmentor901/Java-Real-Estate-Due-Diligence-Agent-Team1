package com.realestate.due_diligence_agent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.dto.AttomAssessmentResponse;
import com.realestate.due_diligence_agent.dto.AttomOwnerResponse;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.JsonNode;

@Service
public class AttomService {

    private final WebClient webClient;
    @PostConstruct
public void checkAttomKey() {
    System.out.println("ATTOM KEY LOADED: " +
            (apiKey != null && !apiKey.isBlank()));

    if (apiKey != null && !apiKey.isBlank()) {
        System.out.println("ATTOM KEY LENGTH: " + apiKey.length());
    }
}

    @Value("${attom.api-key}")
    private String apiKey;

    public AttomService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.gateway.attomdata.com/propertyapi/v1.0.0")
                .build();
    }

    // ---------------------------------------------------------
    // 1. ATTOM Basic Profile
    // ---------------------------------------------------------
    public JsonNode getBasicProfile(String address) {

    try {

        String[] parts = address.split(",", 2);

        String address1 = parts[0].trim();
        String address2 = parts.length > 1
                ? parts[1].trim()
                : "";

        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/property/basicprofile")
                            .queryParam("address1", address1);

                    if (!address2.isBlank()) {
                        uriBuilder.queryParam("address2", address2);
                    }

                    return uriBuilder.build();
                })
                .header("apikey", apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

    } catch (Exception exception) {

        exception.printStackTrace();

        throw new RuntimeException(
                "ATTOM BASIC PROFILE ERROR: "
                        + exception.getMessage(),
                exception
        );
    }
}

    // ---------------------------------------------------------
    // 2. ATTOM Detail Owner
    // ---------------------------------------------------------
   public AttomOwnerResponse getDetailOwner(String address) {

    try {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/property/detailowner")
                        .queryParam("address", address)
                        .build())
                .header("apikey", apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(AttomOwnerResponse.class)
                .block();

    } catch (Exception exception) {

        exception.printStackTrace();

        throw new RuntimeException(
                "ATTOM ERROR: " + exception.getMessage(),
                exception
        );
    }
}

    // ---------------------------------------------------------
    // 3. ATTOM Assessment
    // ---------------------------------------------------------
   public AttomAssessmentResponse getAssessment(String address) {

    try {

        String[] parts = address.split(",", 2);

        String address1 = parts[0].trim();
        String address2 = parts.length > 1
                ? parts[1].trim()
                : "";

        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/assessment/detail")
                            .queryParam("address1", address1);

                    if (!address2.isBlank()) {
                        uriBuilder.queryParam("address2", address2);
                    }

                    return uriBuilder.build();
                })
                .header("apikey", apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(AttomAssessmentResponse.class)
                .block();

    } catch (Exception exception) {

        exception.printStackTrace();

        throw new RuntimeException(
                "ATTOM ASSESSMENT ERROR: " + exception.getMessage(),
                exception
        );
    }
}
}