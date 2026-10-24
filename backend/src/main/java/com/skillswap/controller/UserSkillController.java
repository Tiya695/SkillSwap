package com.skillswap.controller;

import com.skillswap.dto.UserSkillRequest;
import com.skillswap.dto.UserSkillResponse;
import com.skillswap.dto.UserSkillUpdateRequest;
import com.skillswap.enums.SkillType;
import com.skillswap.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/me/skills")
@Tag(name = "My skills", description = "Skills I can teach (OFFERED) and skills I want to learn (WANTED)")
public class UserSkillController {

    private final UserSkillService userSkillService;

    public UserSkillController(UserSkillService userSkillService) {
        this.userSkillService = userSkillService;
    }

    @GetMapping
    @Operation(summary = "List my skills, optionally filtered by type")
    public List<UserSkillResponse> list(Principal principal, @RequestParam(required = false) SkillType type) {
        return userSkillService.list(principal.getName(), type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a skill to my list")
    public UserSkillResponse add(Principal principal, @Valid @RequestBody UserSkillRequest request) {
        return userSkillService.add(principal.getName(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit level or notes")
    public UserSkillResponse update(Principal principal, @PathVariable Long id,
                                    @Valid @RequestBody UserSkillUpdateRequest request) {
        return userSkillService.update(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a skill from my list")
    public void delete(Principal principal, @PathVariable Long id) {
        userSkillService.delete(principal.getName(), id);
    }
}
