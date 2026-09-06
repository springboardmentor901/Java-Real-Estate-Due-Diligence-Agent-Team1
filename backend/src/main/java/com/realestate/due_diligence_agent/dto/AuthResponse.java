package com.realestate.due_diligence_agent.dto;

import com.realestate.due_diligence_agent.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String token;
}