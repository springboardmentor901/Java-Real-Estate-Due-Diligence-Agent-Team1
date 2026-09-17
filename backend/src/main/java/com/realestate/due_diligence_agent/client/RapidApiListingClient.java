package com.realestate.due_diligence_agent.client;

import com.realestate.due_diligence_agent.dto.RapidApiListingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class RapidApiListingClient {

    private final WebClient webClient;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.host}")
    private String rapidApiHost;

    public RapidApiListingResponse searchListings(String location) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(rapidApiHost)
                        .path("/properties/search-buy")
                        .queryParam("location", location)
                        .queryParam("resultsPerPage", 8)
                        .queryParam("page", 1)
                        .queryParam("sortBy", "newest")
                        .queryParam("expandSearchArea", 5)
                        .queryParam("propertyType", "single_family_home")
                        .build())
                .header("X-RapidAPI-Key", rapidApiKey)
                .header("X-RapidAPI-Host", rapidApiHost)
                .retrieve()
                .bodyToMono(RapidApiListingResponse.class)
                .block();
    }
}