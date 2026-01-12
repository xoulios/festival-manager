package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.RejectRequest;
import gr.uoi.festivalmanager.dto.ScheduleRequest;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.service.PerformanceService;
import gr.uoi.festivalmanager.dto.ReviewRequest;
import gr.uoi.festivalmanager.security.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import gr.uoi.festivalmanager.dto.PerformanceViewDto;
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
        Performance created = performanceService.createPerformance(festivalId, principal.getId(), performance);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Performance> update(
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser principal,
        @RequestBody Performance performance
    ) {
    return ResponseEntity.ok(performanceService.updatePerformance(id, principal.getId(), performance));
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<Performance> submit(
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser principal
    ) {
    return ResponseEntity.ok(performanceService.submitPerformance(id, principal.getId()));
    }


    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Performance> withdraw(
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser principal
    ) {
    return ResponseEntity.ok(performanceService.withdrawPerformance(id, principal.getId()));
    }


    @PostMapping("/{id}/review")
    public ResponseEntity<Performance> review(
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser principal,
        @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.ok(performanceService.reviewPerformance(id, principal.getId(), request));
    }

    @PostMapping("/{id}/final-accept")
    public ResponseEntity<Performance> finalAccept(
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser principal
    ) {
        return ResponseEntity.ok(performanceService.finalAccept(id, principal.getId()));
    }

    @PostMapping("/{id}/final-reject")
    public ResponseEntity<Performance> finalReject(
        @PathVariable Long id,
        @AuthenticationPrincipal SecurityUser principal,
        @RequestParam String reason
    ) {
        return ResponseEntity.ok(performanceService.finalReject(id, principal.getId(), reason));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Performance> approve(@PathVariable Long id, @AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(performanceService.approvePerformance(id, principal.getId()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Performance> reject(@PathVariable Long id, @AuthenticationPrincipal SecurityUser principal, @RequestBody RejectRequest request) {
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(performanceService.rejectPerformance(id, principal.getId(), reason));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Performance> schedule(@PathVariable Long id, @AuthenticationPrincipal SecurityUser principal, @RequestBody ScheduleRequest request) {
        String slot = request == null ? null : request.getScheduledSlot();
        return ResponseEntity.ok(performanceService.schedulePerformance(id, principal.getId(), slot));
    }

    @PostMapping("/{id}/final-submit")
    public ResponseEntity<Performance> finalSubmit(@PathVariable Long id, @AuthenticationPrincipal SecurityUser principal, @RequestBody FinalSubmitRequest request) {
        return ResponseEntity.ok(performanceService.finalSubmitPerformance(id, principal.getId(), request));
    }

    @PostMapping("/{id}/assign-handler")
    public ResponseEntity<Performance> assignHandler(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam Long staffId
    ) {
        return ResponseEntity.ok(performanceService.assignHandler(id, principal.getId(), staffId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Performance>> search(
            @RequestParam Long festivalId,
            @RequestParam(name = "q", required = false) String q
    ) {
        return ResponseEntity.ok(performanceService.searchPerformances(festivalId, q));
    }

    @GetMapping("/search-view")
    public ResponseEntity<List<PerformanceViewDto>> searchView(
            @RequestParam Long festivalId,
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(name = "q", required = false) String q
    ) {
        Long userId = (principal == null ? null : principal.getId());
        return ResponseEntity.ok(performanceService.searchPerformancesView(festivalId, userId, q));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<PerformanceViewDto> viewById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        Long userId = (principal == null ? null : principal.getId());
        return ResponseEntity.ok(performanceService.viewPerformanceView(id, userId));
    }

}
