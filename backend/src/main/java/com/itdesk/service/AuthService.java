package com.itdesk.service;

import com.itdesk.dto.request.LoginRequest;
import com.itdesk.dto.request.RefreshTokenRequest;
import com.itdesk.dto.request.RegisterRequest;
import com.itdesk.dto.response.AuthResponse;
import com.itdesk.dto.response.UserProfileResponse;
import com.itdesk.entity.RefreshToken;
import com.itdesk.entity.Role;
import com.itdesk.entity.RoleType;
import com.itdesk.entity.User;
import com.itdesk.exception.DuplicateResourceException;
import com.itdesk.exception.ResourceNotFoundException;
import com.itdesk.repository.DepartmentRepository;
import com.itdesk.repository.RoleRepository;
import com.itdesk.repository.UserRepository;
import com.itdesk.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    // ─── LOGIN ─────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        String accessToken = jwtUtils.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    // ─── REGISTER ──────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role employeeRole = roleRepository.findByName(RoleType.ROLE_EMPLOYEE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found. Please seed roles."));

        Set<Role> roles = new HashSet<>();
        roles.add(employeeRole);

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .jobTitle(request.getJobTitle())
                .phone(request.getPhone())
                .roles(roles)
                .isActive(true)
                .build();

        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .ifPresent(user::setDepartment);
        }

        User savedUser = userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        log.info("New user registered: {}", savedUser.getEmail());
        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }

    // ─── REFRESH TOKEN ─────────────────────────────────────
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findValidToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtils.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Tokens refreshed for user: {}", user.getEmail());
        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    // ─── LOGOUT ────────────────────────────────────────────
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        log.info("User logged out, refresh token revoked.");
    }

    // ─── GET MY PROFILE ────────────────────────────────────
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(User currentUser) {
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated or token is missing/invalid. Please pass Bearer token.");
        }
        User user = userRepository.findById(currentUser.getId()).orElse(currentUser);
        return buildProfileResponse(user);
    }

    // ─── HELPER: AuthResponse Builder ──────────────────────
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .build();
    }

    // ─── HELPER: UserProfileResponse Builder ───────────────
    private UserProfileResponse buildProfileResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .jobTitle(user.getJobTitle())
                .phone(user.getPhone())
                .roles(roles)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .isActive(user.isActive())
                .build();
    }
}
