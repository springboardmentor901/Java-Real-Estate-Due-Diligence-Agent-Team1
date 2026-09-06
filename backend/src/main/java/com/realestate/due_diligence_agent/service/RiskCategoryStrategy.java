package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;

public interface RiskCategoryStrategy {

    RiskAssessment assess(Property property, Report report);
}