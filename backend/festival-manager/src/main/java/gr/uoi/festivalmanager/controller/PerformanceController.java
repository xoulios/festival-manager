package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.PerformanceViewDto;
import gr.uoi.festivalmanager.dto.ReviewRequest;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.security.SecurityUser;
import gr.uoi.festivalmanager.service.PerformanceService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST Controller για τη διαχείριση των performances στο σύστημα διαχείρισης φεστιβάλ.
 * 
 * Παρέχει endpoints για τη δημιουργία, ενημέρωση, υποβολή και διαχείριση του κύκλου ζωής
 * των performances, συμπεριλαμβανομένων των διαδικασιών έγκρισης, προγραμματισμού και αναθεώρησης.
 * 
 * Όλα τα endpoints που τροποποιούν την κατάσταση της performance απαιτούν πιστοποίηση 
 * μέσω @AuthenticationPrincipal. Ο controller χειρίζεται διάφορες καταστάσεις: δημιουργία,
 * υποβολή, έγκριση, απόρριψη, αναθεώρηση, προγραμματισμός και τελική αποδοχή/απόρριψη.
 * 
 * Βασική διαδρομή: /api/performances
 * 
 * Endpoints:
 * - POST /api/performances: Δημιουργία νέας performance για ένα φεστιβάλ
 * - PUT /api/performances/{id}: Ενημέρωση υπάρχουσας performance
 * - POST /api/performances/{id}/submit: Υποβολή performance για αναθεώρηση
 * - DELETE /api/performances/{id}: Απόσυρση μιας performance
 * - POST /api/performances/{id}/approve: Έγκριση μιας performance
 * - POST /api/performances/{id}/reject: Απόρριψη με προαιρετικό λόγο
 * - POST /api/performances/{id}/review: Υποβολή κριτικής για performance
 * - POST /api/performances/{id}/schedule: Προγραμματισμός σε χρονική σχισμή
 * - POST /api/performances/{id}/final-submit: Τελική υποβολή με πρόσθετες λεπτομέρειες
 * - POST /api/performances/{id}/assign-handler: Ανάθεση staff member
 * - POST /api/performances/{id}/final-accept: Τελική αποδοχή
 * - POST /api/performances/{id}/final-reject: Τελική απόρριψη με προαιρετικό λόγο
 * - GET /api/performances/search: Προχωρημένη αναζήτηση με πολλά κριτήρια φίλτρου
 * - GET /api/performances/search-view: Αναζήτηση με άδειες προβολής βάσει χρήστη
 * - GET /api/performances/{id}/view: Λήψη λεπτομερούς προβολής συγκεκριμένης performance
 */

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping
    public ResponseEntity<Performance> create( // Δημιουργία νέας performance
            @RequestParam Long festivalId, // ID φεστιβάλ
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestBody Performance performance) { // Στοιχεία νέας performance
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.createPerformance(festivalId, principal.getId(), performance)); // Δημιουργεί και επιστρέφει την performance
    }

    @PutMapping("/{id}")
    public ResponseEntity<Performance> update(
            @PathVariable Long id, // Performance ID
            @AuthenticationPrincipal SecurityUser principal, // Authenticated user
            @RequestBody Performance performance) { // Updated performance data
        if (principal == null) { // Check for authentication
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return ResponseEntity.ok(performanceService.updatePerformance(id, principal.getId(), performance)); // Update and return
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Performance> submit( // Υποβολή performance για αναθεώρηση
            @PathVariable Long id, // ID της performance που υποβάλλεται
            @AuthenticationPrincipal SecurityUser principal) { // Αυθεντικοποιημένος χρήστης 
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.submitPerformance(id, principal.getId())); // Υποβάλλει την performance και επιστρέφει το αποτέλεσμα
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Performance> withdraw( // Απόσυρση μιας performance
            @PathVariable Long id, // ID της performance που αποσύρεται
            @AuthenticationPrincipal SecurityUser principal) { // Αυθεντικοποιημένος χρήστης
        if (principal == null) {  // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401 
        }
        return ResponseEntity.ok(performanceService.withdrawPerformance(id, principal.getId())); // Αποσύρει την performance και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Performance> approve( // Έγκριση μιας performance
            @PathVariable Long id, // ID της performance που εγκρίνεται
            @AuthenticationPrincipal SecurityUser principal) { // Αυθεντικοποιημένος χρήστης
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.approvePerformance(id, principal.getId())); // Εγκρίνει την performance και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Performance> reject( // Απόρριψη μιας performance
            @PathVariable Long id, // ID της performance που απορρίπτεται
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestParam(required = false) String reason) { // Προαιρετικός λόγος απόρριψης
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.rejectPerformance(id, principal.getId(), reason)); // Απορρίπτει την performance με τον προαιρετικό λόγο και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Performance> review( // Υποβολή κριτικής για μια performance
            @PathVariable Long id, // ID της performance που κρίνεται
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestBody ReviewRequest request) { // Στοιχεία κριτικής
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.reviewPerformance(id, principal.getId(), request)); // Υποβάλλει την κριτική και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Performance> schedule( // Προγραμματισμός μιας performance
            @PathVariable Long id, // ID της performance που προγραμματίζεται
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestParam String scheduledSlot) { // Χρονική σχισμή προγραμματισμού
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.schedulePerformance(id, principal.getId(), scheduledSlot)); // Προγραμματίζει την performance και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/final-submit")  
    public ResponseEntity<Performance> finalSubmit( // Τελική υποβολή μιας performance με πρόσθετες λεπτομέρειες
            @PathVariable Long id, // ID της performance που υποβάλλεται τελικά
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestBody FinalSubmitRequest request) { // Στοιχεία τελικής υποβολής
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.finalSubmitPerformance(id, principal.getId(), request)); // Υποβάλλει τελικά την performance με τις πρόσθετες λεπτομέρειες και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/assign-handler")
    public ResponseEntity<Performance> assignHandler( // Ανάθεση staff member σε μια performance
            @PathVariable Long id, // ID της performance
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestParam Long staffId) { // ID του staff member που ανατίθεται
        if (principal == null) {     // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.assignHandler(id, principal.getId(), staffId)); // Αναθέτει το staff member και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/final-accept")
    public ResponseEntity<Performance> finalAccept( // Τελική αποδοχή μιας performance
            @PathVariable Long id,  // ID της performance που αποδέχεται τελικά
            @AuthenticationPrincipal SecurityUser principal) { // Αυθεντικοποιημένος χρήστης
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.finalAccept(id, principal.getId())); // Αποδέχεται τελικά την performance και επιστρέφει το αποτέλεσμα
    }

    @PostMapping("/{id}/final-reject")
    public ResponseEntity<Performance> finalReject( // Τελική απόρριψη μιας performance
            @PathVariable Long id, // ID της performance που απορρίπτεται τελικά
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestParam(required = false) String reason) { // Προαιρετικός λόγος τελικής απόρριψης
        if (principal == null) { // Έλεγχος για αυθεντικοποίηση
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); // Αν δεν είναι αυθεντικοποιημένος, επιστρέφει 401
        }
        return ResponseEntity.ok(performanceService.finalReject(id, principal.getId(), reason)); // Απορρίπτει τελικά την performance με τον προαιρετικό λόγο και επιστρέφει το αποτέλεσμα
    }

    @GetMapping("/search")
    public ResponseEntity<List<Performance>> search( // Προχωρημένη αναζήτηση performances με πολλά κριτήρια φίλτρου
            @RequestParam Long festivalId,  // ID φεστιβάλ
            @RequestParam(name = "q", required = false) String q,  // Γενικός όρος αναζήτησης
            @RequestParam(name = "name", required = false) String name,  // Όνομα performance
            @RequestParam(name = "genre", required = false) String genre,  // Είδος performance
            @RequestParam(name = "bandMembers", required = false) String bandMembers,  // Μέλη της ορχήστρας
            @RequestParam(name = "state", required = false) String state,  // Κατάσταση performance
            @RequestParam(name = "scheduledFrom", required = false) String scheduledFrom,  // Ημερομηνία προγραμματισμού από
            @RequestParam(name = "scheduledTo", required = false) String scheduledTo, // Ημερομηνία προγραμματισμού έως
            @RequestParam(name = "sortBy", required = false) String sortBy, // Πεδίο ταξινόμησης
            @RequestParam(name = "sortDir", required = false) String sortDir) { // Κατεύθυνση ταξινόμησης
        return ResponseEntity.ok(  // Επιστρέφει HTTP 200 με τη λίστα των performances
                performanceService.searchPerformancesAdvanced(  // Καλεί την υπηρεσία για αναζήτηση με τα δοσμένα κριτήρια
                        festivalId, q, name, genre, bandMembers, state, scheduledFrom, scheduledTo, sortBy, sortDir)); // Επιστρέφει τη λίστα των performances που πληρούν τα κριτήρια
    }

    @GetMapping("/search-view")
    public ResponseEntity<List<PerformanceViewDto>> searchView( // Αναζήτηση performances με άδειες προβολής βάσει χρήστη
            @RequestParam Long festivalId, // ID φεστιβάλ
            @AuthenticationPrincipal SecurityUser principal, // Αυθεντικοποιημένος χρήστης
            @RequestParam(name = "q", required = false) String q,  // Γενικός όρος αναζήτησης
            @RequestParam(name = "name", required = false) String name, // Όνομα performance
            @RequestParam(name = "genre", required = false) String genre, // Είδος performance
            @RequestParam(name = "bandMembers", required = false) String bandMembers, // Μέλη της ορχήστρας
            @RequestParam(name = "state", required = false) String state, // Κατάσταση performance
            @RequestParam(name = "scheduledFrom", required = false) String scheduledFrom, // Ημερομηνία προγραμματισμού από
            @RequestParam(name = "scheduledTo", required = false) String scheduledTo, // Ημερομηνία προγραμματισμού έως
            @RequestParam(name = "sortBy", required = false) String sortBy, // Πεδίο ταξινόμησης
            @RequestParam(name = "sortDir", required = false) String sortDir) { // Κατεύθυνση ταξινόμησης
        Long userId = (principal == null ? null : principal.getId()); // Λαμβάνει το ID του χρήστη ή null αν δεν είναι αυθεντικοποιημένος
        return ResponseEntity.ok( // Επιστρέφει HTTP 200 με τη λίστα των performances με άδειες προβολής
                performanceService.searchPerformancesViewAdvanced( // Καλεί την υπηρεσία για αναζήτηση με άδειες προβολής
                        festivalId, userId, q, name, genre, bandMembers, state, scheduledFrom, scheduledTo, sortBy,
                        sortDir)); // Επιστρέφει τη λίστα των performances που πληρούν τα κριτήρια με άδειες προβολής
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<PerformanceViewDto> view( // Λήψη λεπτομερούς προβολής συγκεκριμένης performance
            @PathVariable Long id, // ID της performance που προβάλλεται
            @AuthenticationPrincipal SecurityUser principal) { // Αυθεντικοποιημένος χρήστης
        Long userId = (principal == null ? null : principal.getId()); // Λαμβάνει το ID του χρήστη ή null αν δεν είναι αυθεντικοποιημένος
        return ResponseEntity.ok(performanceService.viewPerformanceView(id, userId)); // Επιστρέφει την λεπτομερή προβολή της performance
    }
}
