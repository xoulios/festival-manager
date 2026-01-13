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

/**
 * REST Controller για τη διαχείριση των festivals στο σύστημα διαχείρισης φεστιβάλ.
 * 
 * Παρέχει endpoints για τη δημιουργία, ενημέρωση, αναζήτηση και διαχείριση του κύκλου ζωής
 * των festivals, συμπεριλαμβανομένων των διαδικασιών αλλαγής κατάστασης, ανάθεσης ρόλων
 * και προγραμματισμού αποφάσεων.
 * 
 * Όλα τα endpoints που τροποποιούν την κατάσταση του festival απαιτούν πιστοποίηση 
 * μέσω @AuthenticationPrincipal. Ο controller χειρίζεται διάφορες καταστάσεις: δημιουργία,
 * ενημέρωση, αναζήτηση, αλλαγή κατάστασης, ανάθεση ρόλων και μετάβαση σε απόφαση.
 * 
 * Βασική διαδρομή: /api/festivals
 * 
 * Endpoints:
 * - POST /api/festivals: Δημιουργία νέου festival
 * - PUT /api/festivals/{id}: Ενημέρωση υπάρχοντος festival
 * - GET /api/festivals/search-view: Αναζήτηση festivals με άδειες προβολής βάσει χρήστη
 * - GET /api/festivals/{id}/view: Λήψη λεπτομερούς προβολής συγκεκριμένου festival
 * - GET /api/festivals: Λήψη λίστας όλων των festivals
 * - GET /api/festivals/{id}: Λήψη συγκεκριμένου festival
 * - DELETE /api/festivals/{id}: Διαγραφή festival
 * - PATCH /api/festivals/{id}/state: Αλλαγή κατάστασης festival
 * - POST /api/festivals/{festivalId}/roles: Ανάθεση ρόλου σε χρήστη
 * - POST /api/festivals/{festivalId}/assign-role: Ανάθεση ρόλου (legacy endpoint)
 * - POST /api/festivals/{id}/decision: Μετάβαση σε κατάσταση απόφασης
 */

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalService festivalService; // Υπηρεσία διαχείρισης festivals

    public FestivalController(FestivalService festivalService) { // Constructor injection
        this.festivalService = festivalService;
    }

    @PostMapping
    public ResponseEntity<FestivalResponse> createFestival( // Δημιουργία νέου festival
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @Valid @RequestBody FestivalCreateRequest request // Στοιχεία νέου festival
    ) {
        FestivalResponse created = festivalService.createFestival(request, principal.getId()); // Δημιουργεί το festival
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // Επιστρέφει HTTP 201 με τα στοιχεία του νέου festival
    }

    @PutMapping("/{id}")
    public ResponseEntity<FestivalResponse> updateFestival( // Ενημέρωση υπάρχοντος festival
            @PathVariable Long id, // ID του festival που ενημερώνεται
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @Valid @RequestBody FestivalUpdateRequest request // Ενημερωμένα στοιχεία festival
    ) {
        return ResponseEntity.ok(festivalService.updateFestival(id, principal.getId(), request)); // Ενημερώνει και επιστρέφει το festival
    }

    @GetMapping("/search-view")
    public ResponseEntity<List<FestivalViewDto>> searchViewFestivals( // Αναζήτηση festivals με άδειες προβολής βάσει χρήστη
            @RequestParam(required = false) String title, // Τίτλος festival (προαιρετικό)
            @RequestParam(required = false) String description, // Περιγραφή festival (προαιρετική)
            @RequestParam(required = false) LocalDate startDateFrom, // Ημερομηνία έναρξης από (προαιρετική)
            @RequestParam(required = false) LocalDate startDateTo, // Ημερομηνία έναρξης έως (προαιρετική)
            @RequestParam(required = false) LocalDate endDateFrom, // Ημερομηνία λήξης από (προαιρετική)
            @RequestParam(required = false) LocalDate endDateTo, // Ημερομηνία λήξης έως (προαιρετική)
            @RequestParam(required = false) String performanceTitle, // Τίτλος performance (προαιρετικό)
            @AuthenticationPrincipal SecurityUser principal // Αυθεντικοποιημένος χρήστης
    ) {
        Long viewerUserId = (principal == null) ? null : principal.getId(); // Λαμβάνει το ID του χρήστη ή null αν δεν είναι αυθεντικοποιημένος
        return ResponseEntity.ok( // Επιστρέφει HTTP 200 με τη λίστα των festivals
                festivalService.searchView( // Καλεί την υπηρεσία για αναζήτηση με άδειες προβολής
                        viewerUserId,
                        title,
                        description,
                        startDateFrom,
                        startDateTo,
                        endDateFrom,
                        endDateTo,
                        performanceTitle
                )
        ); // Επιστρέφει τη λίστα των festivals που πληρούν τα κριτήρια με άδειες προβολής
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<FestivalViewDto> viewFestival( // Λήψη λεπτομερούς προβολής συγκεκριμένου festival
            @PathVariable Long id, // ID του festival που προβάλλεται
            @AuthenticationPrincipal SecurityUser principal // Αυθεντικοποιημένος χρήστης
    ) {
        Long viewerUserId = (principal == null) ? null : principal.getId(); // Λαμβάνει το ID του χρήστη ή null αν δεν είναι αυθεντικοποιημένος
        return ResponseEntity.ok(festivalService.viewFestival(viewerUserId, id)); // Επιστρέφει την λεπτομερή προβολή του festival
    }

    @GetMapping
    public ResponseEntity<List<FestivalResponse>> listFestivals() { // Λήψη λίστας όλων των festivals
        return ResponseEntity.ok(festivalService.listFestivals()); // Επιστρέφει HTTP 200 με τη λίστα όλων των festivals
    }

    @GetMapping("/{id}")
    public ResponseEntity<FestivalResponse> getFestival(@PathVariable Long id) { // Λήψη συγκεκριμένου festival
        return ResponseEntity.ok(festivalService.getFestival(id)); // Επιστρέφει HTTP 200 με τα στοιχεία του festival
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFestival( // Διαγραφή festival
            @PathVariable Long id, // ID του festival που διαγράφεται
            @AuthenticationPrincipal SecurityUser principal // Αυθεντικοποιημένος χρήστης
    ) {
        festivalService.deleteFestival(id, principal.getId()); // Διαγράφει το festival
        return ResponseEntity.noContent().build(); // Επιστρέφει HTTP 204 No Content
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<FestivalResponse> changeState( // Αλλαγή κατάστασης festival
            @PathVariable Long id, // ID του festival
            @RequestParam("state") String state, // Νέα κατάσταση festival
            @AuthenticationPrincipal SecurityUser principal // Αυθεντικοποιημένος χρήστης
    ) {
        FestivalState newState = FestivalState.valueOf(state.trim().toUpperCase()); // Μετατροπή string κατάστασης σε enum
        FestivalResponse updated = festivalService.changeState(id, principal.getId(), newState); // Αλλάζει την κατάσταση του festival
        return ResponseEntity.ok(updated); // Επιστρέφει HTTP 200 με το ενημερωμένο festival
    }

    @PostMapping("/{festivalId}/roles")
    public ResponseEntity<Void> assignRole( // Ανάθεση ρόλου σε χρήστη για συγκεκριμένο festival
            @PathVariable Long festivalId, // ID του festival
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @Valid @RequestBody AssignRoleRequest request // Στοιχεία ανάθεσης ρόλου
    ) {
        festivalService.assignRole(festivalId, principal.getId(), request); // Αναθέτει τον ρόλο
        return ResponseEntity.noContent().build(); // Επιστρέφει HTTP 204 No Content
    }

    @PostMapping("/{festivalId}/assign-role")
    public ResponseEntity<Void> assignRoleLegacy( // Ανάθεση ρόλου (legacy endpoint για συμβατότητα)
            @PathVariable Long festivalId, // ID του festival
            @RequestParam("userId") Long userId, // ID του χρήστη
            @RequestParam("roleId") Long roleId // ID του ρόλου
    ) {
        festivalService.assignRole(festivalId, userId, roleId); // Αναθέτει τον ρόλο
        return ResponseEntity.ok().build(); // Επιστρέφει HTTP 200 OK
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<FestivalResponse> moveToDecision( // Μετάβαση festival σε κατάσταση απόφασης
            @PathVariable Long id, // ID του festival
            @AuthenticationPrincipal SecurityUser principal // Αυθεντικοποιημένος χρήστης
    ) {
        return ResponseEntity.ok(festivalService.moveToDecision(id, principal.getId())); // Μετακινεί το festival στο στάδιο απόφασης και επιστρέφει το ενημερωμένο αποτέλεσμα
    }
}
