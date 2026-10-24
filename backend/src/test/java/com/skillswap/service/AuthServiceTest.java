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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    static final LocalDate DOB = LocalDate.of(2000, 5, 17);

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtil jwtUtil;
    @InjectMocks
    AuthService authService;

    private User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Tiya");
        user.setEmail("tiya@gmail.com");
        user.setPassword("OLD_HASH");
        user.setDateOfBirth(DOB);
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void registerSavesUserWithHashedPasswordAndDateOfBirth() {
        when(userRepository.existsByEmail("tiya@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret1!")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest("Tiya", "  Tiya@Gmail.com ", DOB, "Secret1!"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("HASHED");
        assertThat(saved.getValue().getEmail()).isEqualTo("tiya@gmail.com");
        assertThat(saved.getValue().getDateOfBirth()).isEqualTo(DOB);
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(response.email()).isEqualTo("tiya@gmail.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("tiya@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Tiya", "tiya@gmail.com", DOB, "Secret1!")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenAndUser() {
        when(userRepository.findByEmail("tiya@gmail.com")).thenReturn(Optional.of(existingUser()));
        when(jwtUtil.generateToken("tiya@gmail.com", "USER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("tiya@gmail.com", "Secret1!"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().name()).isEqualTo("Tiya");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("tiya@gmail.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void resetPasswordStoresNewHashWhenDateOfBirthMatches() {
        User user = existingUser();
        when(userRepository.findByEmail("tiya@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("NEW_HASH");

        MessageResponse response = authService.resetPassword(new ForgotPasswordRequest("Tiya@gmail.com", DOB, "NewPass1!"));

        assertThat(user.getPassword()).isEqualTo("NEW_HASH");
        assertThat(response.message()).contains("Password updated");
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordRejectsWrongDateOfBirth() {
        User user = existingUser();
        when(userRepository.findByEmail("tiya@gmail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(
                new ForgotPasswordRequest("tiya@gmail.com", LocalDate.of(1999, 1, 1), "NewPass1!")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
        assertThat(user.getPassword()).isEqualTo("OLD_HASH");
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordGivesTheSameAnswerForUnknownEmail() {
        when(userRepository.findByEmail("nobody@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ForgotPasswordRequest("nobody@gmail.com", DOB, "NewPass1!")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void resetPasswordLocksAfterFiveWrongAttempts() {
        when(userRepository.findByEmail("tiya@gmail.com")).thenReturn(Optional.of(existingUser()));
        ForgotPasswordRequest wrong = new ForgotPasswordRequest("tiya@gmail.com", LocalDate.of(1990, 1, 1), "NewPass1!");

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.resetPassword(wrong)).isInstanceOf(BadRequestException.class);
        }

        assertThatThrownBy(() -> authService.resetPassword(new ForgotPasswordRequest("tiya@gmail.com", DOB, "NewPass1!")))
                .isInstanceOf(TooManyRequestsException.class);
    }
}
