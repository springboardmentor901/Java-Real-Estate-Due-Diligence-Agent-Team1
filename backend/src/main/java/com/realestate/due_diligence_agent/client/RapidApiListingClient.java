package com.realestate.due_diligence_agent.client;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class RapidApiListingClient {

    private final WebClient webClient;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.host}")
    private String rapidApiHost;

    private static final long COOLDOWN_MILLIS = 60_000L; // 60 seconds cooldown
    private volatile long cooldownUntil = 0L;

    public boolean isCooldownActive() {
        return System.currentTimeMillis() < cooldownUntil;
    }

    private void triggerCooldown(String reason) {
        cooldownUntil = System.currentTimeMillis() + COOLDOWN_MILLIS;
        System.err.println("RapidAPI rate limit/quota reached (" + reason + "). Entering 60s cooldown until "
                + Instant.ofEpochMilli(cooldownUntil) + ". Serving persisted comparables.");
    }

    public JsonNode searchListings(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        if (isCooldownActive()) {
            System.out.println("RapidAPI client is currently in rate-limit cooldown. Skipping live search for: " + location);
            return null;
        }

        String locationId = resolveLocationId(location);

        if (isCooldownActive()) {
            return null;
        }

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(rapidApiHost)
                            .path("/properties/search-buy")
                            .queryParam("location", locationId)
                            .queryParam("resultsPerPage", 8)
                            .queryParam("page", 1)
                            .queryParam("sortBy", "newest")
                            .build())
                    .header("X-RapidAPI-Key", rapidApiKey)
                    .header("X-RapidAPI-Host", rapidApiHost)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                triggerCooldown("HTTP 429 Too Many Requests on search-buy");
            } else {
                System.err.println("RapidAPI search-buy HTTP error " + e.getStatusCode() + " for location '" + locationId + "': " + e.getMessage());
            }
            return null;
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("Too Many Requests"))) {
                triggerCooldown("429 Too Many Requests in message");
            } else {
                System.err.println("RapidAPI search-buy failed for location '" + locationId + "': " + e.getMessage());
            }
            return null;
        }
    }

    public String resolveLocationId(String location) {
        if (isCooldownActive()) {
            return location;
        }

        try {
            JsonNode autoResp = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(rapidApiHost)
                            .path("/properties/auto-complete")
                            .queryParam("input", location)
                            .build())
                    .header("X-RapidAPI-Key", rapidApiKey)
                    .header("X-RapidAPI-Host", rapidApiHost)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (autoResp != null) {
                JsonNode items = autoResp.path("data").path("autocomplete");
                if (items.isArray() && !items.isEmpty()) {
                    for (JsonNode item : items) {
                        String id = item.path("_id").asText(null);
                        if (id != null && !id.isBlank()) {
                            System.out.println("RapidAPI auto-complete resolved '" + location + "' -> '" + id + "'");
                            return id;
                        }
                    }
                }
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                triggerCooldown("HTTP 429 Too Many Requests on auto-complete");
            } else {
                System.err.println("RapidAPI auto-complete HTTP error " + e.getStatusCode() + " for '" + location + "': " + e.getMessage());
            }
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("Too Many Requests"))) {
                triggerCooldown("429 Too Many Requests in message");
            } else {
                System.err.println("RapidAPI auto-complete lookup failed for '" + location + "': " + e.getMessage());
            }
        }
        return location;
    }
}
