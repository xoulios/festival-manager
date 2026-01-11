package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.AssignRoleRequest;
import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.dto.FestivalUpdateRequest;
import gr.uoi.festivalmanager.dto.FestivalViewDto;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.security.SecurityUser;
import gr.uoi.festivalmanager.service.FestivalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @PostMapping
    public ResponseEntity<FestivalResponse> createFestival(
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @RequestBody FestivalCreateRequest request
    ) {
        FestivalResponse created = festivalService.createFestival(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FestivalResponse> updateFestival(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @RequestBody FestivalUpdateRequest request
    ) {
        return ResponseEntity.ok(festivalService.updateFestival(id, principal.getId(), request));
    }

    @GetMapping("/search-view")
    public ResponseEntity<List<FestivalViewDto>> searchViewFestivals(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(required = false) LocalDate endDateFrom,
            @RequestParam(required = false) LocalDate endDateTo,
            @RequestParam(required = false) String performanceTitle,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        Long viewerUserId = (principal == null) ? null : principal.getId();
        return ResponseEntity.ok(
                festivalService.searchView(
                        viewerUserId,
                        title,
                        description,
                        startDateFrom,
                        startDateTo,
                        endDateFrom,
                        endDateTo,
                        performanceTitle
                )
        );
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<FestivalViewDto> viewFestival(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        Long viewerUserId = (principal == null) ? null : principal.getId();
        return ResponseEntity.ok(festivalService.viewFestival(viewerUserId, id));
    }

    @GetMapping
    public ResponseEntity<List<FestivalResponse>> listFestivals() {
        return ResponseEntity.ok(festivalService.listFestivals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FestivalResponse> getFestival(@PathVariable Long id) {
        return ResponseEntity.ok(festivalService.getFestival(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFestival(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        festivalService.deleteFestival(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<FestivalResponse> changeState(
            @PathVariable Long id,
            @RequestParam("state") String state,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        FestivalState newState = FestivalState.valueOf(state.trim().toUpperCase());
        FestivalResponse updated = festivalService.changeState(id, principal.getId(), newState);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{festivalId}/roles")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        festivalService.assignRole(festivalId, principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{festivalId}/assign-role")
    public ResponseEntity<Void> assignRoleLegacy(
            @PathVariable Long festivalId,
            @RequestParam("userId") Long userId,
            @RequestParam("roleId") Long roleId
    ) {
        festivalService.assignRole(festivalId, userId, roleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<FestivalResponse> moveToDecision(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        return ResponseEntity.ok(festivalService.moveToDecision(id, principal.getId()));
    }
}
