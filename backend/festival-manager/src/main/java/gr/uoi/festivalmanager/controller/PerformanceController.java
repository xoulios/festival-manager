package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.PerformanceViewDto;
import gr.uoi.festivalmanager.dto.ReviewRequest;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.security.SecurityUser;
import gr.uoi.festivalmanager.service.PerformanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping
    public ResponseEntity<Performance> create(
            @RequestParam Long festivalId,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestBody Performance performance
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.createPerformance(festivalId, principal.getId(), performance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Performance> update(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestBody Performance performance
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.updatePerformance(id, principal.getId(), performance));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Performance> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.submitPerformance(id, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Performance> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.withdrawPerformance(id, principal.getId()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Performance> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.approvePerformance(id, principal.getId()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Performance> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false) String reason
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.rejectPerformance(id, principal.getId(), reason));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Performance> review(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestBody ReviewRequest request
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.reviewPerformance(id, principal.getId(), request));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Performance> schedule(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam String scheduledSlot
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.schedulePerformance(id, principal.getId(), scheduledSlot));
    }

    @PostMapping("/{id}/final-submit")
    public ResponseEntity<Performance> finalSubmit(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestBody FinalSubmitRequest request
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.finalSubmitPerformance(id, principal.getId(), request));
    }

    @PostMapping("/{id}/assign-handler")
    public ResponseEntity<Performance> assignHandler(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam Long staffId
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.assignHandler(id, principal.getId(), staffId));
    }

    @PostMapping("/{id}/final-accept")
    public ResponseEntity<Performance> finalAccept(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.finalAccept(id, principal.getId()));
    }

    @PostMapping("/{id}/final-reject")
    public ResponseEntity<Performance> finalReject(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false) String reason
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok(performanceService.finalReject(id, principal.getId(), reason));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Performance>> search(
            @RequestParam Long festivalId,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "bandMembers", required = false) String bandMembers,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDir", required = false) String sortDir
    ) {
        return ResponseEntity.ok(
                performanceService.searchPerformancesAdvanced(
                        festivalId, q, name, genre, bandMembers, state, sortBy, sortDir
                )
        );
    }

    @GetMapping("/search-view")
    public ResponseEntity<List<PerformanceViewDto>> searchView(
            @RequestParam Long festivalId,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "bandMembers", required = false) String bandMembers,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDir", required = false) String sortDir
    ) {
        Long userId = (principal == null ? null : principal.getId());
        return ResponseEntity.ok(
                performanceService.searchPerformancesViewAdvanced(
                        festivalId, userId, q, name, genre, bandMembers, state, sortBy, sortDir
                )
        );
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<PerformanceViewDto> view(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        Long userId = (principal == null ? null : principal.getId());
        return ResponseEntity.ok(performanceService.viewPerformanceView(id, userId));
    }
}
