package com.realestate.due_diligence_agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.ChangePasswordRequest;
import com.realestate.due_diligence_agent.dto.UpdateUserProfileRequest;
import com.realestate.due_diligence_agent.dto.UserProfileResponse;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // GET USER PROFILE
    // =====================================================

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal User user) {

        UserProfileResponse response =
                new UserProfileResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt()
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE USER PROFILE
    // =====================================================

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserProfileRequest request) {

        if (request.getFullName() != null
                && !request.getFullName().isBlank()) {

            user.setFullName(
                    request.getFullName().trim()
            );
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            String newEmail =
                    request.getEmail().trim();

            if (!newEmail.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(newEmail)) {

                return ResponseEntity
                        .badRequest()
                        .body("Email is already registered by another user.");
            }

            user.setEmail(newEmail);
        }

        User updatedUser =
                userRepository.save(user);

        UserProfileResponse response =
                new UserProfileResponse(
                        updatedUser.getId(),
                        updatedUser.getFullName(),
                        updatedUser.getEmail(),
                        updatedUser.getRole(),
                        updatedUser.getCreatedAt()
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @PutMapping("/profile/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody ChangePasswordRequest request) {

        // -------------------------------------------------
        // Validate request
        // -------------------------------------------------

        if (request.getCurrentPassword() == null
                || request.getCurrentPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Current password is required.");
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("New password is required.");
        }

        // -------------------------------------------------
        // Check current password
        // -------------------------------------------------

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body("Current password is incorrect.");
        }

        // -------------------------------------------------
        // Prevent same password
        // -------------------------------------------------

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body("New password must be different from the current password.");
        }

        // -------------------------------------------------
        // Encode new password
        // -------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        // -------------------------------------------------
        // Save password
        // -------------------------------------------------

        userRepository.save(user);

        return ResponseEntity.ok(
                "Password changed successfully."
        );
    }
}