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
public class PermitsClient {

    private final WebClient webClient;

    @Value("${shovels.api.key}")
    private String apiKey;

    public PermitsClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public JsonNode getPermits(String address) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.shovels.ai")
                            .path("/v1/permits/search")
                            .queryParam("address", address)
                            .build())
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("Shovels API call failed", e);
        }
    }
}
