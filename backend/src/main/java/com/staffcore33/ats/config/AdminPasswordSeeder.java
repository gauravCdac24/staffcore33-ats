package com.staffcore33.ats.config;

import com.staffcore33.ats.user.User;
import com.staffcore33.ats.user.UserRepository;
import com.staffcore33.ats.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminPasswordSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@staffcore33.com").ifPresentOrElse(user -> {
            user.setPasswordHash(passwordEncoder.encode("Admin@123"));
            user.setRole(UserRole.ADMIN);
            user.setActive(true);
            userRepository.save(user);
            log.info("Ensured admin password for admin@staffcore33.com");
        }, () -> {
            User admin = User.builder()
                    .email("admin@staffcore33.com")
                    .fullName("System Admin")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .build();
            userRepository.save(admin);
            log.info("Created default admin admin@staffcore33.com / Admin@123");
        });
    }
}
