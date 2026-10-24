package com.skillswap.controller;

import com.skillswap.dto.DeleteAccountRequest;
import com.skillswap.dto.PublicProfileResponse;
import com.skillswap.dto.UpdateProfileRequest;
import com.skillswap.dto.UserResponse;
import com.skillswap.service.AccountService;
import com.skillswap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/me")
    @Operation(summary = "Current user profile")
    public UserResponse me(Principal principal) {
        return userService.getMe(principal.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile")
    public UserResponse updateMe(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(principal.getName(), request);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload my profile photo (PNG, JPG or WEBP, up to 2 MB)")
    public UserResponse uploadAvatar(Principal principal, @RequestPart("file") MultipartFile file) {
        return userService.updateAvatar(principal.getName(), file);
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Remove my profile photo")
    public UserResponse removeAvatar(Principal principal) {
        return userService.removeAvatar(principal.getName());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete my account and all my data (password required)")
    public void deleteMe(Principal principal, @Valid @RequestBody DeleteAccountRequest request) {
        accountService.deleteAccount(principal.getName(), request.password());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Public profile with skills and average rating")
    public PublicProfileResponse profile(@PathVariable Long id) {
        return userService.getPublicProfile(id);
    }
}
