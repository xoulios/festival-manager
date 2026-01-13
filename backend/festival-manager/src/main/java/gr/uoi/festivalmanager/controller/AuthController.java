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

    private final UserFestivalRoleRepository userFestivalRoleRepository; // Αποθετήριο για τους ρόλους χρηστών ανά φεστιβάλ

    public AuthController(UserFestivalRoleRepository userFestivalRoleRepository) { // Constructor injection
        this.userFestivalRoleRepository = userFestivalRoleRepository; // Αρχικοποίηση του αποθετηρίου
    }

    @GetMapping("/me") // Λήψη πληροφοριών για τον αυθεντικοποιημένο χρήστη
    public ResponseEntity<AuthMeResponse> me(@AuthenticationPrincipal SecurityUser principal) { // Αυθεντικοποιημένος χρήστης
    if (principal == null) { // Έλεγχος αν ο χρήστης είναι αυθεντικοποιημένος
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Επιστροφή 401 Unauthorized αν δεν είναι αυθεντικοποιημένος
    }

    List<AuthMeResponse.FestivalRole> festivalRoles = 
            userFestivalRoleRepository.findFestivalRolesForUserId(principal.getId()); // Λήψη ρόλων χρήστη ανά φεστιβάλ

    String effectiveRole = computeEffectiveRole(festivalRoles); // Υπολογισμός του πιο σημαντικού ρόλου του χρήστη

    AuthMeResponse body = new AuthMeResponse(
            principal.getId(),
            principal.getUsername(),
            effectiveRole,
            festivalRoles
    ); // Δημιουργία του αντικειμένου απόκρισης με τα στοιχεία του χρήστη και τους ρόλους του

    return ResponseEntity.ok(body); // Επιστροφή 200 OK με τα στοιχεία του χρήστη
    }


    private String computeEffectiveRole(List<AuthMeResponse.FestivalRole> roles) { // Υπολογισμός του πιο σημαντικού ρόλου από τη λίστα ρόλων   
        List<String> priority = List.of("ORGANIZER", "STAFF", "PROGRAMMER", "ARTIST", "SUBMITTER"); // Προτεραιότητα ρόλων

        for (String p : priority) { // Επανάληψη στην προτεραιότητα ρόλων
            for (AuthMeResponse.FestivalRole r : roles) { // Επανάληψη στους ρόλους του χρήστη
                if (p.equalsIgnoreCase(r.getRole())) { // Έλεγχος αν ο ρόλος ταιριάζει με την προτεραιότητα
                    return p; // Επιστροφή του πιο σημαντικού ρόλου
                }
            }
        }

        return roles.isEmpty() ? "NONE" : roles.get(0).getRole(); // Επιστροφή "NONE" αν δεν υπάρχουν ρόλοι, αλλιώς ο πρώτος ρόλος
    }
}
