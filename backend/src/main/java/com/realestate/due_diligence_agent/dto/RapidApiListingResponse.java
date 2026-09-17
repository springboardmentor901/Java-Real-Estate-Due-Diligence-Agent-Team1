package com.realestate.due_diligence_agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RapidApiListingResponse {

    private Data data;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private Integer count;

        private Integer total;

        private List<Listing> results;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Listing {

        @JsonProperty("property_id")
        private String propertyId;

        @JsonProperty("listing_id")
        private String listingId;

        private String status;

        private Location location;

        @JsonProperty("list_price")
        private Double listPrice;

        @JsonProperty("list_date")
        private Instant listDate;

        private String href;

        private Description description;

        private Source source;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {

        private Address address;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {

        private String line;

        private String city;

        @JsonProperty("postal_code")
        private String postalCode;

        @JsonProperty("state_code")
        private String stateCode;

        private String state;

        private String country;

        private Coordinate coordinate;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinate {

        private Double lat;

        private Double lon;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Description {

        private Integer beds;

        private Integer baths;

        private Integer sqft;

        @JsonProperty("lot_sqft")
        private Integer lotSqft;

        private String type;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {

        private String type;

        private String name;

        private String id;

        @JsonProperty("listing_id")
        private String listingId;
    }
}