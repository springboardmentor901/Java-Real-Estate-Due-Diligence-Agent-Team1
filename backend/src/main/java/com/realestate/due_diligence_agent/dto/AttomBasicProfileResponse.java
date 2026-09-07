package com.realestate.due_diligence_agent.dto;

import lombok.Data;

@Data
public class AttomBasicProfileResponse {
    private Property property;

    @Data
    public static class Property {
        private Identifier identifier;
        private Location location;
        private Summary summary;
    }

    @Data
    public static class Identifier {
        private String obPropId;
    }

    @Data
    public static class Location {
        private String latitude;
        private String longitude;
    }

    @Data
    public static class Summary {
        private String propclass;
        private String propsubtype;
        private int yearbuilt;
    }
}
