package com.realestate.due_diligence_agent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.realestate.due_diligence_agent.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class FloodZoneClient {

    private final WebClient webClient;

    @Value("${fema.nfhl.base.url}")
    private String baseUrl;

    public FloodZoneClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public JsonNode getFloodZoneData(String lat, String lon) {
        try {
            return webClient.get()
                    .uri(baseUrl + "/28/query?geometry=" + lon + "," + lat + "&geometryType=esriGeometryPoint&spatialRel=esriSpatialRelIntersects&outFields=*&returnGeometry=false&f=json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("FEMA NFHL call failed", e);
        }
    }
}
