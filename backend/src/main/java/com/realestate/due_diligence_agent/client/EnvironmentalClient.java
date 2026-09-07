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
public class EnvironmentalClient {

    private final WebClient webClient;

    @Value("${epa.envirofacts.base.url}")
    private String baseUrl;

    public EnvironmentalClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public JsonNode getEnvironmentalRecords(String address) {
        try {
            return webClient.get()
                    .uri(baseUrl + "/FRS/ADDRESS/" + address + "/JSON")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("EPA Envirofacts call failed", e);
        }
    }
}
