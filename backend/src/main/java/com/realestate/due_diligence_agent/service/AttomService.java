package com.realestate.due_diligence_agent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.dto.AttomAssessmentResponse;
import com.realestate.due_diligence_agent.dto.AttomOwnerResponse;
import com.realestate.due_diligence_agent.util.AddressParser;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.JsonNode;

@Service
public class AttomService {

    private final WebClient webClient;

    @PostConstruct
    public void checkAttomKey() {
        if (apiKey == null || apiKey.length() != 32) {
            try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (in != null) {
                    java.util.Properties props = new java.util.Properties();
                    props.load(in);
                    String fileKey = props.getProperty("attom.api-key");
                    if (fileKey != null && fileKey.trim().length() == 32) {
                        this.apiKey = fileKey.trim();
                        System.out.println("ATTOM KEY restored from application.properties");
                    }
                }
            } catch (Exception ignored) {}
        }

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
        if (address == null || address.isBlank()) {
            return null;
        }

        AddressParser parsed = AddressParser.parse(address);
        if (!parsed.isUsAddress()) {
            return null;
        }

        try {
            String address1 = parsed.getAddress1();
            String address2 = parsed.getAddress2();

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
            System.err.println("ATTOM BASIC PROFILE ERROR for address '" + address + "': " + exception.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------
    // 2. ATTOM Detail Owner
    // ---------------------------------------------------------
    public AttomOwnerResponse getDetailOwner(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        AddressParser parsed = AddressParser.parse(address);
        if (!parsed.isUsAddress()) {
            return null;
        }

        try {
            String address1 = parsed.getAddress1();
            String address2 = parsed.getAddress2();

            return webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .path("/property/detailowner")
                                .queryParam("address1", address1);

                        if (!address2.isBlank()) {
                            uriBuilder.queryParam("address2", address2);
                        }

                        return uriBuilder.build();
                    })
                    .header("apikey", apiKey)
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(AttomOwnerResponse.class)
                    .block();

        } catch (Exception exception) {
            System.err.println("ATTOM OWNER ERROR for address '" + address + "': " + exception.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------
    // 3. ATTOM Assessment
    // ---------------------------------------------------------
    public AttomAssessmentResponse getAssessment(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        AddressParser parsed = AddressParser.parse(address);
        if (!parsed.isUsAddress()) {
            return null;
        }

        try {
            String address1 = parsed.getAddress1();
            String address2 = parsed.getAddress2();

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
            System.err.println("ATTOM ASSESSMENT ERROR for address '" + address + "': " + exception.getMessage());
            return null;
        }
    }
}