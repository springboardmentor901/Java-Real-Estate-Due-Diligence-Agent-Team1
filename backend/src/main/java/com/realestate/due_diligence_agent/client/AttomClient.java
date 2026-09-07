package com.realestate.due_diligence_agent.client;

import com.realestate.due_diligence_agent.dto.AttomAssessmentResponse;
import com.realestate.due_diligence_agent.dto.AttomBasicProfileResponse;
import com.realestate.due_diligence_agent.dto.AttomDetailOwnerResponse;
import com.realestate.due_diligence_agent.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class AttomClient {

    private final WebClient webClient;

    @Value("${attom.api.key}")
    private String apiKey;

    public AttomClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public AttomBasicProfileResponse getBasicProfile(String address) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.gateway.attomdata.com")
                            .path("/propertyapi/v1.0.0/property/basicprofile")
                            .queryParam("address", address)
                            .build())
                    .header("apikey", apiKey)
                    .header("accept", "application/json")
                    .retrieve()
                    .bodyToMono(AttomBasicProfileResponse.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("ATTOM Basic Profile call failed", e);
        }
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public AttomDetailOwnerResponse getDetailOwner(String address) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.gateway.attomdata.com")
                            .path("/propertyapi/v1.0.0/property/detailowner")
                            .queryParam("address", address)
                            .build())
                    .header("apikey", apiKey)
                    .header("accept", "application/json")
                    .retrieve()
                    .bodyToMono(AttomDetailOwnerResponse.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("ATTOM Detail Owner call failed", e);
        }
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public AttomAssessmentResponse getAssessment(String address) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.gateway.attomdata.com")
                            .path("/propertyapi/v1.0.0/assessment/detail")
                            .queryParam("address", address)
                            .build())
                    .header("apikey", apiKey)
                    .header("accept", "application/json")
                    .retrieve()
                    .bodyToMono(AttomAssessmentResponse.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("ATTOM Assessment call failed", e);
        }
    }
}
