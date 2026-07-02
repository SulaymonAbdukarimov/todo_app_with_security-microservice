package uz.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.auth.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
//        if (!userRepository.existsByEmail("admin@todo.uz")) {
//            User admin = User.builder()
//                    .fullName("Admin User")
//                    .email("admin@todo.uz")
//                    .password(passwordEncoder.encode("admin123"))
//                    .role(Role.USER)
//                    .build();
//
//            userRepository.save(admin);
//            log.info("✅ Default user created: admin@todo.uz / admin123");
//        } else {
//            log.info("ℹ️ Default user already exists, skipping...");
//        }
    }
}
