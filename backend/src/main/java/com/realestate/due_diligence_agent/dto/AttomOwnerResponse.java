package com.realestate.due_diligence_agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class AttomOwnerResponse {

    private Status status;
    private List<Property> property;

    @Data
    public static class Status {
        private String version;
        private Integer code;
        private String msg;
        private Integer total;
        private Integer page;
        private Integer pagesize;
        private String responseDateTime;
        private String transactionID;
        private Long attomId;
    }

    @Data
    public static class Property {
        private Owner owner;
    }

    @Data
    public static class Owner {
        private OwnerDetails owner1;
        private String ownerrelationshiprightscode;
        private String absenteeownerstatus;
    }

    @Data
    public static class OwnerDetails {
        private String fullname;
        private String lastname;
        private String firstnameandmi;
    }
}