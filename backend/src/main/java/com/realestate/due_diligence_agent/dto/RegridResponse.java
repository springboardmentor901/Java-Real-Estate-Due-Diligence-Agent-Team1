package com.realestate.due_diligence_agent.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RegridResponse {

    private FeatureCollection parcels;

    private FeatureCollection buildings;

    private FeatureCollection zoning;


    // =========================================================
    // Feature Collection
    // =========================================================

    @Data
    public static class FeatureCollection {

        private String type;

        private List<Feature> features;
    }


    // =========================================================
    // Feature
    // =========================================================

    @Data
    public static class Feature {

        private String type;

        private Properties properties;

        private Object geometry;

        private Long id;
    }


    // =========================================================
    // Properties
    // =========================================================

    @Data
    public static class Properties {

        private String headline;

        private String path;

        private Fields fields;

        private Context context;

        private List<Address> addresses;

        private List<Object> enhanced_ownership;

        @JsonProperty("ll_uuid")
        private String llUuid;
    }


    // =========================================================
    // Fields
    // =========================================================

    @Data
    public static class Fields {

        // -------------------------
        // Zoning
        // -------------------------

        private String zoning;

        @JsonProperty("zoning_description")
        private String zoningDescription;

        @JsonProperty("zoning_type")
        private String zoningType;

        @JsonProperty("zoning_subtype")
        private String zoningSubtype;

        @JsonProperty("zoning_code_link")
        private String zoningCodeLink;

        @JsonProperty("zoning_id")
        private Long zoningId;


        // -------------------------
        // Property information
        // -------------------------

        private String address;

        private String city;

        private String county;

        @JsonProperty("state2")
        private String state;

        private String szip;

        private String owner;


        // -------------------------
        // Parcel information
        // -------------------------

        private String parcelnumb;

        private String parcelnumbNoFormatting;

        private String stateParcelnumb;

        private String usecode;

        private String usedesc;


        // -------------------------
        // Location
        // -------------------------

        private String lat;

        private String lon;


        // -------------------------
        // Regrid identifiers
        // -------------------------

        private String path;

        @JsonProperty("ll_uuid")
        private String llUuid;
    }


    // =========================================================
    // Context
    // =========================================================

    @Data
    public static class Context {

        private String headline;

        private String name;

        private String path;

        private Boolean active;
    }


    // =========================================================
    // Address
    // =========================================================

    @Data
    public static class Address {

        private String a_id;

        private String a_address;

        private String a_saddno;

        private String a_saddpref;

        private String a_saddstr;

        private String a_saddsttyp;

        private String a_saddstsuf;

        private String a_sunit;

        private String a_szip5;

        private String a_szip;

        private String a_scity;

        private String a_state2;

        private String a_county;

        private String a_lat;

        private String a_lon;

        @JsonProperty("ll_uuid")
        private String llUuid;
    }
}