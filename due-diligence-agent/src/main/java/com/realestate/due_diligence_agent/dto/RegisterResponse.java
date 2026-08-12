package com.realestate.due_diligence_agent.dto;
 
import com.realestate.due_diligence_agent.entity.Role;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
 
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
