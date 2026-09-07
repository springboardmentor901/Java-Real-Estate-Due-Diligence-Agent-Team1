package com.realestate.due_diligence_agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class AttomDetailOwnerResponse {
    private List<Property> property;

    @Data
    public static class Property {
        private Owner owner;
    }

    @Data
    public static class Owner {
        private String owner1last;
        private String owner1first;
        private String ownertypedesc;
    }
}
