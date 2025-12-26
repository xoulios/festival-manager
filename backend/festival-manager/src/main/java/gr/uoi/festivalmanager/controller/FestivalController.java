package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.service.FestivalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @PostMapping("/{festivalId}/assign-role")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long festivalId,
            @RequestParam Long userId,
            @RequestParam Long roleId
    ) {
        festivalService.assignRole(festivalId, userId, roleId);
        return ResponseEntity.ok().build();
    }
}
