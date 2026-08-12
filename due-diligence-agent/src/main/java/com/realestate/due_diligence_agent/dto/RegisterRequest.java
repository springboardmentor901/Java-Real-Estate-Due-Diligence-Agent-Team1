package com.realestate.due_diligence_agent.dto;
 
import com.realestate.due_diligence_agent.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
 
    @NotBlank(message = "Full name is required")
    private String fullName;
 
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
 
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
 
    @NotNull(message = "Role is required")
    private Role role;
}
