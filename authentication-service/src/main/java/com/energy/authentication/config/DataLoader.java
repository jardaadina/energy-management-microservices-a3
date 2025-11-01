package com.energy.authentication.config;

import com.energy.authentication.entity.User;
import com.energy.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception
    {
        if (!userRepository.existsByUsername("admin"))
        {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setUserId(1L);
            userRepository.save(admin);
            log.info("Default admin user created: username=admin, password=admin123");
        }

        if (!userRepository.existsByUsername("client"))
        {
            User client = new User();
            client.setUsername("client");
            client.setPassword(passwordEncoder.encode("client123"));
            client.setRole("CLIENT");
            client.setUserId(2L);
            userRepository.save(client);
            log.info("Default client user created: username=client, password=client123");
        }

        log.info("Data initialization completed");
    }
}