package com.skillswap.controller;

import com.skillswap.dto.ReviewRequest;
import com.skillswap.dto.ReviewResponse;
import com.skillswap.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/swaps/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Review the other person after a completed swap")
    public ReviewResponse add(Principal principal, @PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviewService.add(principal.getName(), id, request);
    }

    @GetMapping("/users/{id}/reviews")
    @Operation(summary = "Reviews received by a user")
    public List<ReviewResponse> forUser(@PathVariable Long id) {
        return reviewService.listForUser(id);
    }
}
