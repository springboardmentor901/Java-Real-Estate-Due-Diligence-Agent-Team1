package com.realestate.due_diligence_agent.dto;
 
import com.realestate.due_diligence_agent.entity.Role;
import lombok.*;
 
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
 
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String token;
}
