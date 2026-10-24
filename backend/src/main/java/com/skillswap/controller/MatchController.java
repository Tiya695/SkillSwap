package com.skillswap.controller;

import com.skillswap.dto.MatchResponse;
import com.skillswap.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    @Operation(summary = "Mutual and one-way matches for me, best first")
    public List<MatchResponse> matches(Principal principal) {
        return matchService.findMatches(principal.getName());
    }
}
