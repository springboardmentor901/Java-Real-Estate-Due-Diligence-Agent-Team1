package com.realestate.due_diligence_agent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AttomAssessmentResponse {

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
        private Assessment assessment;
    }

    @Data
    public static class Assessment {
        private Assessed assessed;
        private Tax tax;
    }

    @Data
    public static class Assessed {
        private BigDecimal assdttlvalue;
    }

    @Data
    public static class Tax {
        private BigDecimal taxamt;
        private Integer taxyear;
    }
}