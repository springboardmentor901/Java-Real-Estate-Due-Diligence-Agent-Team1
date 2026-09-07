package com.realestate.due_diligence_agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.RegridResponse;
import com.realestate.due_diligence_agent.service.RegridService;

@RestController
@RequestMapping("/api/regrid")
public class RegridController {

    private final RegridService regridService;

    public RegridController(RegridService regridService) {
        this.regridService = regridService;
    }

    @GetMapping("/parcel")
    public RegridResponse getParcelData(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        return regridService.getParcelData(latitude, longitude);
    }
}