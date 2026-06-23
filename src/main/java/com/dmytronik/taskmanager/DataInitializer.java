package com.dmytronik.taskmanager;

import com.dmytronik.taskmanager.model.Role;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.username}")
    private String adminUsername;

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User user = new User();
        user.setUsername(adminUsername);
        user.setEmail(adminEmail);
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setRole(Role.ADMIN);

        userRepository.save(user);
    }
}
