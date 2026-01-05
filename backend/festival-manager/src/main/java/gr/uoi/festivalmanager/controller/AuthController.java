package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.AuthMeResponse;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserFestivalRoleRepository userFestivalRoleRepository;

    public AuthController(UserRepository userRepository, UserFestivalRoleRepository userFestivalRoleRepository) {
        this.userRepository = userRepository;
        this.userFestivalRoleRepository = userFestivalRoleRepository;
    }

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authentication principal");
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        Long userId = user.getId();

        List<Object[]> rows = userFestivalRoleRepository.findFestivalRoles(userId);
        List<AuthMeResponse.FestivalRole> festivalRoles = new ArrayList<>();

        boolean hasProgrammer = false;
        boolean hasStaff = false;
        boolean hasSubmitter = false;

        for (Object[] r : rows) {
            Long festivalId = (Long) r[0];
            String role = (String) r[1];

            festivalRoles.add(new AuthMeResponse.FestivalRole(festivalId, role));

            if ("PROGRAMMER".equalsIgnoreCase(role)) hasProgrammer = true;
            else if ("STAFF".equalsIgnoreCase(role)) hasStaff = true;
            else if ("SUBMITTER".equalsIgnoreCase(role)) hasSubmitter = true;
        }

        String effectiveRole;
        if (hasProgrammer) effectiveRole = "PROGRAMMER";
        else if (hasStaff) effectiveRole = "STAFF";
        else if (hasSubmitter) effectiveRole = "SUBMITTER";
        else effectiveRole = "SUBMITTER";

        return new AuthMeResponse(userId, username, effectiveRole, festivalRoles);
    }
}
