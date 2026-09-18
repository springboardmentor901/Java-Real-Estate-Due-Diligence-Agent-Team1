package com.realestate.due_diligence_agent.dto;

import java.time.LocalDateTime;

import com.realestate.due_diligence_agent.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}