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

    private static final String DEFAULT_ADMIN_EMAIL="akshaya@gmail.com";
    private static final String DEFAULT_ADMIN_PASSWORD="Akshaya@123";

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)){
            return;
        }

        User admin=User.builder()
                .fullName("Default Adminstarator")
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMINISTRATOR)
                .build();

        userRepository.save(admin);
        log.info("Seeded default adminstrator account:{}",DEFAULT_ADMIN_EMAIL);

    }

}
