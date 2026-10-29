package com.itdesk.config;

import com.itdesk.entity.Department;
import com.itdesk.entity.Role;
import com.itdesk.entity.RoleType;
import com.itdesk.entity.User;
import com.itdesk.repository.DepartmentRepository;
import com.itdesk.repository.RoleRepository;
import com.itdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultDepartmentAndAdmin();
    }

    private void seedRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepository.findByName(roleType).isEmpty()) {
                Role role = Role.builder()
                        .name(roleType)
                        .description(getRoleDescription(roleType))
                        .build();
                roleRepository.save(role);
                log.info("Seeded role: {}", roleType.name());
            }
        }
    }

    private void seedDefaultDepartmentAndAdmin() {
        Department itDept = null;
        if (departmentRepository.count() == 0) {
            itDept = Department.builder()
                    .name("IT Support")
                    .code("IT")
                    .build();
            itDept = departmentRepository.save(itDept);
            log.info("Seeded default department: IT Support");
        } else {
            itDept = departmentRepository.findAll().get(0);
        }

        String adminEmail = "admin@itdesk.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName(RoleType.ROLE_SUPER_ADMIN).ifPresent(roles::add);
            roleRepository.findByName(RoleType.ROLE_IT_ADMIN).ifPresent(roles::add);

            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@123456"))
                    .firstName("Super")
                    .lastName("Admin")
                    .jobTitle("System Administrator")
                    .department(itDept)
                    .roles(roles)
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("Seeded super admin user: {}", adminEmail);
        }
    }

    private String getRoleDescription(RoleType roleType) {
        return switch (roleType) {
            case ROLE_EMPLOYEE -> "Standard employee with ticket creation rights";
            case ROLE_IT_AGENT -> "Support technician resolving tickets";
            case ROLE_IT_ADMIN -> "IT Lead managing team and catalog";
            case ROLE_SUPER_ADMIN -> "Full platform administrator";
        };
    }
}
