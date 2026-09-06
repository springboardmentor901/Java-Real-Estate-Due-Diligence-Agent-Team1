package com.realestate.due_diligence_agent.dto;

import java.util.List;

import lombok.Data;

@Data
public class ShovelsPermitResponse {

    private List<PermitData> items;

    private Integer size;

    private String next_cursor;

    private Integer total_count;
    

    @Data
    public static class PermitData {

        private String id;

        private String number;

        private String description;

        private String description_derived;

        private String jurisdiction;

        private Integer job_value;

        private String type;

        private String subtype;

        private Integer fees;

        private String status;

        private String file_date;

        private String issue_date;

        private String final_date;

        private String start_date;

        private String end_date;

        private Integer total_duration;

        private Integer construction_duration;

        private Integer approval_duration;

        private Double inspection_pass_rate;

        private String contractor_id;

        private List<String> tags;

        private Address address;

        private GeoIds geo_ids;
    }

    @Data
    public static class Address {

        private String street_no;

        private String street;

        private String city;

        private String county;

        private String zip_code;

        private String zip_code_ext;

        private String state;

        private String jurisdiction;

        private List<Double> latlng;
    }

    @Data
    public static class GeoIds {

        private String address_id;

        private String city_id;

        private String county_id;

        private String jurisdiction_id;
    }
}