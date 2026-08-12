package com.realestate.due_diligence_agent.seeder;
 
import com.realestate.due_diligence_agent.entity.Role;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
 
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
 
    private static final String DEFAULT_ADMIN_EMAIL = "admin@realestateapp.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
 
    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            return; // already seeded - safe to run on every restart
        }
 
        User admin = User.builder()
                .fullName("Default Administrator")
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMINISTRATOR)
                .build();
 
        userRepository.save(admin);
        log.info("Seeded default administrator account: {}", DEFAULT_ADMIN_EMAIL);
    }
}
