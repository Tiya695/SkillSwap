package com.skillswap.service;

import com.skillswap.dto.AuthResponse;
import com.skillswap.dto.ForgotPasswordRequest;
import com.skillswap.dto.LoginRequest;
import com.skillswap.dto.MessageResponse;
import com.skillswap.dto.RegisterRequest;
import com.skillswap.dto.UserResponse;
import com.skillswap.entity.User;
import com.skillswap.enums.Role;
import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.TooManyRequestsException;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered",
                    Map.of("email", "Email is already registered"));
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setDateOfBirth(request.dateOfBirth());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        return UserResponse.from(userRepository.save(user));
    }

    public MessageResponse resetPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        checkNotLocked(email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getDateOfBirth() == null || !user.getDateOfBirth().equals(request.dateOfBirth())) {
            recordFailure(email);
            throw new BadRequestException("Email and date of birth do not match our records");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        failures.remove(email);
        return new MessageResponse("Password updated. You can log in with your new password.");
    }

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    private void checkNotLocked(String email) {
        Deque<Instant> recent = failures.get(email);
        if (recent == null) {
            return;
        }
        synchronized (recent) {
            recent.removeIf(t -> t.isBefore(Instant.now().minus(LOCK_TIME)));
            if (recent.size() >= MAX_FAILURES) {
                throw new TooManyRequestsException("Too many wrong attempts. Please try again in 15 minutes.");
            }
        }
    }

    private void recordFailure(String email) {
        Deque<Instant> recent = failures.computeIfAbsent(email, e -> new ArrayDeque<>());
        synchronized (recent) {
            recent.addLast(Instant.now());
        }
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getRole().name()),
                UserResponse.from(user));
    }
}
