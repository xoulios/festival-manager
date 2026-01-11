package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.AuthMeResponse;
import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.security.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserFestivalRoleRepository userFestivalRoleRepository;

    public AuthController(UserFestivalRoleRepository userFestivalRoleRepository) {
        this.userFestivalRoleRepository = userFestivalRoleRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(@AuthenticationPrincipal SecurityUser principal) {
    if (principal == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    List<AuthMeResponse.FestivalRole> festivalRoles =
            userFestivalRoleRepository.findFestivalRolesForUserId(principal.getId());

    String effectiveRole = computeEffectiveRole(festivalRoles);

    AuthMeResponse body = new AuthMeResponse(
            principal.getId(),
            principal.getUsername(),
            effectiveRole,
            festivalRoles
    );

    return ResponseEntity.ok(body);
    }


    private String computeEffectiveRole(List<AuthMeResponse.FestivalRole> roles) {
        List<String> priority = List.of("ORGANIZER", "STAFF", "PROGRAMMER", "ARTIST", "SUBMITTER");

        for (String p : priority) {
            for (AuthMeResponse.FestivalRole r : roles) {
                if (p.equalsIgnoreCase(r.getRole())) {
                    return p;
                }
            }
        }

        return roles.isEmpty() ? "NONE" : roles.get(0).getRole();
    }
}
