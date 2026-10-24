package com.skillswap.controller;

import com.skillswap.dto.PageResponse;
import com.skillswap.dto.SkillRequest;
import com.skillswap.dto.SkillResponse;
import com.skillswap.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Skill catalog. Anyone logged in can add a skill; only admins edit or delete.")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @Operation(summary = "List skills (search and filter by category, paginated)")
    public PageResponse<SkillResponse> list(@RequestParam(required = false) String category,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "12") int size) {
        return skillService.list(category, q, page, size);
    }

    @GetMapping("/categories")
    @Operation(summary = "List the category names in use")
    public List<String> categories() {
        return skillService.categories();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one skill")
    @ApiResponse(responseCode = "404", description = "Skill not found")
    public SkillResponse get(@PathVariable Long id) {
        return skillService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new skill to the catalog (any logged-in user)")
    @ApiResponse(responseCode = "409", description = "Name already exists")
    public SkillResponse create(@Valid @RequestBody SkillRequest request) {
        return skillService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a skill (admin)")
    public SkillResponse update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a skill (admin)")
    @ApiResponse(responseCode = "409", description = "Skill is still in use")
    public void delete(@PathVariable Long id) {
        skillService.delete(id);
    }
}
