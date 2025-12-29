package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.RejectRequest;
import gr.uoi.festivalmanager.dto.ScheduleRequest;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.service.PerformanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam Long artistId,
            @RequestBody Performance performance
    ) {
        Performance created = performanceService.createPerformance(festivalId, artistId, performance);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Performance> update(@PathVariable Long id, @RequestBody Performance performance) {
        return ResponseEntity.ok(performanceService.updatePerformance(id, performance));
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<Performance> submit(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.submitPerformance(id));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Performance> withdraw(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.withdrawPerformance(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Performance> review(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(performanceService.reviewPerformance(id, userId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Performance> approve(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(performanceService.approvePerformance(id, userId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Performance> reject(@PathVariable Long id, @RequestParam Long userId, @RequestBody RejectRequest request) {
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(performanceService.rejectPerformance(id, userId, reason));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Performance> schedule(@PathVariable Long id, @RequestParam Long userId, @RequestBody ScheduleRequest request) {
        String slot = request == null ? null : request.getScheduledSlot();
        return ResponseEntity.ok(performanceService.schedulePerformance(id, userId, slot));
    }

    @PostMapping("/{id}/final-submit")
    public ResponseEntity<Performance> finalSubmit(@PathVariable Long id, @RequestParam Long userId, @RequestBody FinalSubmitRequest request) {
        return ResponseEntity.ok(performanceService.finalSubmitPerformance(id, userId, request));
    }
}
