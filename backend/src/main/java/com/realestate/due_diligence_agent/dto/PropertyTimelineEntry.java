package com.realestate.due_diligence_agent.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PropertyTimelineEntry {

    private LocalDate date;
    private String label;
    private String description;
}