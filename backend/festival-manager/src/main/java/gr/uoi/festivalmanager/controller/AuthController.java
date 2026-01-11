package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.FestivalRoleDto;
import gr.uoi.festivalmanager.dto.MeResponse;
import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserFestivalRoleRepository userFestivalRoleRepository;

    public AuthController(UserFestivalRoleRepository userFestivalRoleRepository) {
        this.userFestivalRoleRepository = userFestivalRoleRepository;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal SecurityUser principal) {
        Long userId = principal.getId();

        List<UserFestivalRole> roles = userFestivalRoleRepository.findAllByUserId(userId);
        List<FestivalRoleDto> festivalRoles = roles.stream()
                .map(ufr -> new FestivalRoleDto(
                        ufr.getFestival().getId(),
                        ufr.getRole() == null ? null : ufr.getRole().getName()
                ))
                .toList();

        String effective = computeEffectiveRole(festivalRoles);

        return new MeResponse(userId, principal.getUsername(), effective, festivalRoles);
    }

    private String computeEffectiveRole(List<FestivalRoleDto> festivalRoles) {
        boolean hasProgrammer = false;
        boolean hasStaff = false;
        boolean hasSubmitter = false;

        for (FestivalRoleDto r : festivalRoles) {
            if (r == null || r.getRole() == null) continue;
            String name = r.getRole().trim().toUpperCase();
            if (name.contains("PROGRAMMER") || name.contains("ORGANIZER")) hasProgrammer = true;
            else if (name.contains("STAFF")) hasStaff = true;
            else if (name.contains("SUBMITTER") || name.contains("ARTIST")) hasSubmitter = true;
        }

        if (hasProgrammer) return "PROGRAMMER";
        if (hasStaff) return "STAFF";
        if (hasSubmitter) return "SUBMITTER";
        return "VISITOR";
    }
}
