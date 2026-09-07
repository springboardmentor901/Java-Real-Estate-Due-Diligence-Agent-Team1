package com.realestate.due_diligence_agent.client;

import com.realestate.due_diligence_agent.dto.RegridParcelResponse;
import com.realestate.due_diligence_agent.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class RegridClient {

    private final WebClient webClient;

    @Value("${regrid.api.token}")
    private String apiToken;

    public RegridClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = ExternalApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public RegridParcelResponse getParcelInfo(String lat, String lon) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("app.regrid.com")
                            .path("/api/v1/parcels")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("token", apiToken)
                            .build())
                    .retrieve()
                    .bodyToMono(RegridParcelResponse.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw new ExternalApiException("Regrid Parcel API call failed", e);
        }
    }
}
