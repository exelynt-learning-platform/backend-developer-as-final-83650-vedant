package com.resourcebooking.config;

import com.resourcebooking.entity.Resource;
import com.resourcebooking.entity.User;
import com.resourcebooking.enums.Role;
import com.resourcebooking.repository.ResourceRepository;
import com.resourcebooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// runs once when the app starts up, just inserts a couple of test users + resources
// so I don't have to manually insert rows every time I reset the db while testing
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ResourceRepository resourceRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "Admin User", Role.ADMIN);
            userRepository.save(admin);
            System.out.println("seeded admin user -> username: admin / password: admin123");
        }

        if (!userRepository.existsByUsername("john")) {
            User john = new User("john", passwordEncoder.encode("user123"), "John Doe", Role.USER);
            userRepository.save(john);
            System.out.println("seeded normal user -> username: john / password: user123");
        }

        if (!userRepository.existsByUsername("mary")) {
            User mary = new User("mary", passwordEncoder.encode("user123"), "Mary Smith", Role.USER);
            userRepository.save(mary);
            System.out.println("seeded normal user -> username: mary / password: user123");
        }

        if (resourceRepository.count() == 0) {
            resourceRepository.save(new Resource("Conference Room A", "ROOM",
                    "Large meeting room with projector", "Building 1, Floor 2", true));
            resourceRepository.save(new Resource("Conference Room B", "ROOM",
                    "Small meeting room", "Building 1, Floor 3", true));
            resourceRepository.save(new Resource("Company Car - Swift", "VEHICLE",
                    "5 seater, manual", "Basement Parking", true));
            resourceRepository.save(new Resource("Projector - Epson EB-X05", "EQUIPMENT",
                    "Portable projector, needs to be booked separately from rooms", "Store Room", true));
            System.out.println("seeded 4 sample resources");
        }
    }
}
