package com.realestate.due_diligence_agent.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeocodingService {

	private final RestTemplate restTemplate;

	public GeocodingService(RestTemplate restTemplate) {
	    this.restTemplate = restTemplate;
	}

    public GeocodingResult geocode(String address) {

        String url = "https://nominatim.openstreetmap.org/search"
                + "?q=" + address.replace(" ", "+")
                + "&format=json"
                + "&limit=1";

        GeocodingResult[] results =
                restTemplate.getForObject(url, GeocodingResult[].class);

        if (results == null || results.length == 0) {
            throw new RuntimeException("Address could not be found");
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