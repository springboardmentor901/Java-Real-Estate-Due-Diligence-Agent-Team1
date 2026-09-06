package com.realestate.due_diligence_agent.service;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;

    public GeocodingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GeocodingResult geocode(String address) {

        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                HttpHeaders.USER_AGENT,
                "DueDiligenceAgent/1.0 (real-estate-due-diligence)"
        );

        RequestEntity<Void> request =
                new RequestEntity<>(
                        headers,
                        HttpMethod.GET,
                        uri
                );

        ResponseEntity<GeocodingResult[]> response =
                restTemplate.exchange(
                        request,
                        GeocodingResult[].class
                );

        GeocodingResult[] results = response.getBody();

        if (results == null || results.length == 0) {
            throw new RuntimeException(
                    "Address could not be found: " + address
            );
        }

        return results[0];
    }

    public static class GeocodingResult {

        private String lat;
        private String lon;
        private String display_name;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }

        public String getDisplay_name() {
            return display_name;
        }

        public void setDisplay_name(String display_name) {
            this.display_name = display_name;
        }
    }
}