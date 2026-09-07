package com.realestate.due_diligence_agent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AttomAssessmentResponse {
    private List<Property> property;

    @Data
    public static class Property {
        private Assessment assessment;
    }

    @Data
    public static class Assessment {
        private Tax tax;
        private Assessed assessed;
    }

    @Data
    public static class Tax {
        private Integer taxyear;
        private BigDecimal taxamt;
    }

    @Data
    public static class Assessed {
        private BigDecimal assdttlvalue;
    }
}
