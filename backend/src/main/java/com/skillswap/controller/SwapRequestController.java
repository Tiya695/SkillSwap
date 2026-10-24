package com.skillswap.controller;

import com.skillswap.dto.PageResponse;
import com.skillswap.dto.SwapCreateRequest;
import com.skillswap.dto.SwapResponse;
import com.skillswap.dto.SwapStatusRequest;
import com.skillswap.enums.SwapStatus;
import com.skillswap.service.SwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/swaps")
@Tag(name = "Swap requests")
public class SwapRequestController {

    private final SwapService swapService;

    public SwapRequestController(SwapService swapService) {
        this.swapService = swapService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a swap request")
    @ApiResponse(responseCode = "409", description = "A pending request for this swap already exists")
    public SwapResponse create(Principal principal, @Valid @RequestBody SwapCreateRequest request) {
        return swapService.create(principal.getName(), request);
    }

    @GetMapping
    @Operation(summary = "My incoming or outgoing requests (paginated)")
    public PageResponse<SwapResponse> list(Principal principal,
                                           @RequestParam(defaultValue = "incoming") String box,
                                           @RequestParam(required = false) SwapStatus status,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return swapService.list(principal.getName(), box, status, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "One request (only if I am a participant)")
    public SwapResponse get(Principal principal, @PathVariable Long id) {
        return swapService.get(principal.getName(), id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Accept, reject, cancel or complete a request")
    @ApiResponse(responseCode = "409", description = "That status change is not allowed right now")
    public SwapResponse updateStatus(Principal principal, @PathVariable Long id,
                                     @Valid @RequestBody SwapStatusRequest request) {
        return swapService.updateStatus(principal.getName(), id, request.status());
    }
}
