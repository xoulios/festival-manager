package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.PerformanceViewDto;
import gr.uoi.festivalmanager.dto.ReviewRequest;
import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.entity.Review;
import gr.uoi.festivalmanager.entity.Role;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.enums.PerformanceState;
import gr.uoi.festivalmanager.exception.BusinessRuleException;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import gr.uoi.festivalmanager.repository.PerformanceRepository;
import gr.uoi.festivalmanager.repository.ReviewRepository;
import gr.uoi.festivalmanager.repository.RoleRepository;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final UserFestivalRoleRepository userFestivalRoleRepository;
    private final RoleRepository roleRepository;
    private final ReviewRepository reviewRepository;

    public PerformanceServiceImpl(
            PerformanceRepository performanceRepository,
            FestivalRepository festivalRepository,
            UserRepository userRepository,
            UserFestivalRoleRepository userFestivalRoleRepository,
            RoleRepository roleRepository,
            ReviewRepository reviewRepository
    ) {
        this.performanceRepository = performanceRepository;
        this.festivalRepository = festivalRepository;
        this.userRepository = userRepository;
        this.userFestivalRoleRepository = userFestivalRoleRepository;
        this.roleRepository = roleRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Performance updatePerformance(
            Long performanceId,
            Performance updated
    ) {
        throw new BusinessRuleException("Authentication required");
    }

    @Override
    public Performance submitPerformance(Long performanceId) {
        throw new BusinessRuleException("Authentication required");
    }

    @Override
    public Performance withdrawPerformance(Long performanceId) {
        throw new BusinessRuleException("Authentication required");
    }

    @Override
    public Performance createPerformance(
            Long festivalId,
            Long artistId,
            Performance performance
    ) {
        Festival festival = festivalRepository
                .findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        ensureSubmitterRole(festivalId, artistId);

        User artist = userRepository
                .findById(artistId)
                .orElseThrow(() -> new BusinessRuleException("Artist not found"));

        performance.setFestival(festival);
        performance.setArtist(artist);
        performance.setState(PerformanceState.CREATED);

        return performanceRepository.save(performance);
    }

    @Override
    public Performance updatePerformance(
            Long performanceId,
            Long artistId,
            Performance updated
    ) {
        Performance existing = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        requireSubmitterOwner(existing, artistId);

        if (existing.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException(
                    "Only CREATED performances can be updated"
            );
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setGenre(updated.getGenre());
        existing.setBandMembers(updated.getBandMembers());
        existing.setTechnicalRequirements(updated.getTechnicalRequirements());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setSetlist(updated.getSetlist());
        existing.setPreferredTimeSlots(updated.getPreferredTimeSlots());
        existing.setPreferredRehearsalTimes(updated.getPreferredRehearsalTimes());

        return performanceRepository.save(existing);
    }

    @Override
    public Performance submitPerformance(Long performanceId, Long artistId) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        requireSubmitterOwner(p, artistId);

        if (p.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException(
                    "Only CREATED performances can be submitted"
            );
        }

        Festival festival = requireFestival(p);
        if (festival.getState() != FestivalState.SUBMISSION) {
            throw new BusinessRuleException("Festival is not in SUBMISSION state");
        }

        p.setState(PerformanceState.SUBMITTED);
        return performanceRepository.save(p);
    }

    @Override
    public Performance withdrawPerformance(Long performanceId, Long artistId) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        requireSubmitterOwner(p, artistId);

        if (p.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException(
                    "Only CREATED performances can be withdrawn"
            );
        }

        performanceRepository.delete(p);
        return p;
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceViewDto viewPerformanceView(
            Long performanceId,
            Long userId
    ) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        PerformanceViewDto dto = toViewDto(p, userId);
        if (dto == null) {
            throw new BusinessRuleException("Performance not found");
        }
        return dto;
    }

    @Override
    @Transactional
    public Performance reviewPerformance(
            Long performanceId,
            Long staffId,
            ReviewRequest request
    ) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);
        requireRole(staffId, festival.getId(), "STAFF");

        if (p.getHandler() == null
                || p.getHandler().getId() == null
                || !p.getHandler().getId().equals(staffId)) {
            throw new BusinessRuleException(
                    "Only the assigned handler can review this performance"
            );
        }
        if (festival.getState() != FestivalState.REVIEW) {
            throw new BusinessRuleException(
                    "Review allowed only when festival is in REVIEW state"
            );
        }
        if (p.getState() != PerformanceState.SUBMITTED) {
            throw new BusinessRuleException(
                    "Only SUBMITTED performances can be reviewed"
            );
        }
        if (request == null) {
            throw new BusinessRuleException("Review payload is required");
        }
        if (request.getScore() < 0 || request.getScore() > 10) {
            throw new BusinessRuleException("score must be between 0 and 10");
        }
        if (request.getComments() == null || request.getComments().trim().isEmpty()) {
            throw new BusinessRuleException("comments are required");
        }

        User reviewer = userRepository
                .findById(staffId)
                .orElseThrow(() -> new BusinessRuleException("Reviewer not found"));

        Review review = new Review();
        review.setPerformance(p);
        review.setReviewer(reviewer);
        review.setScore(request.getScore());
        review.setComments(request.getComments().trim());
        reviewRepository.save(review);

        p.setState(PerformanceState.REVIEWED);
        p.setReviewedAt(LocalDateTime.now());

        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance approvePerformance(Long performanceId, Long staffId) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);

        boolean isProgrammer = hasRole(staffId, festival.getId(), "PROGRAMMER");
        if (!isProgrammer) {
            requireSubmitterOwner(p, staffId);
        }

        if (festival.getState() != FestivalState.SCHEDULING) {
            throw new BusinessRuleException(
                    "Approval allowed only when festival is in SCHEDULING state"
            );
        }
        if (p.getState() != PerformanceState.REVIEWED) {
            throw new BusinessRuleException(
                    "Only REVIEWED performances can be approved"
            );
        }

        p.setState(PerformanceState.APPROVED);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance rejectPerformance(Long performanceId, Long staffId, String reason) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);

        requireRole(staffId, festival.getId(), "PROGRAMMER");

        if (festival.getState() != FestivalState.SCHEDULING) {
            throw new BusinessRuleException(
                    "Rejection allowed only when festival is in SCHEDULING state"
            );
        }
        if (p.getState() != PerformanceState.REVIEWED) {
            throw new BusinessRuleException(
                    "Only REVIEWED performances can be rejected"
            );
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Rejection reason is required");
        }

        User reviewer = userRepository
                .findById(staffId)
                .orElseThrow(() -> new BusinessRuleException("Reviewer not found"));

        Review review = new Review();
        review.setPerformance(p);
        review.setReviewer(reviewer);
        review.setScore(0);
        review.setComments("REJECT: " + reason.trim());
        reviewRepository.save(review);

        p.setRejectionReason(reason.trim());
        p.setState(PerformanceState.REJECTED);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance schedulePerformance(
            Long performanceId,
            Long schedulerId,
            String scheduledSlot
    ) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);

        boolean isOrganizer = hasRole(schedulerId, festival.getId(), "ORGANIZER");
        boolean isStaff = hasRole(schedulerId, festival.getId(), "STAFF");
        boolean isProgrammer = hasRole(schedulerId, festival.getId(), "PROGRAMMER");
        if (!isOrganizer && !isStaff) {
            throw new BusinessRuleException(
                    "Only ORGANIZER or STAFF can schedule performances"
            );
        }

        if (festival.getState() != FestivalState.SCHEDULING) {
            throw new BusinessRuleException(
                    "Scheduling allowed only when festival is in SCHEDULING state"
            );
        }
        if (p.getState() != PerformanceState.APPROVED) {
            throw new BusinessRuleException(
                    "Only APPROVED performances can be scheduled"
            );
        }
        if (scheduledSlot == null || scheduledSlot.trim().isEmpty()) {
            throw new BusinessRuleException("scheduledSlot is required");
        }

        p.setScheduledSlot(scheduledSlot.trim());

        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance finalSubmitPerformance(
            Long performanceId,
            Long artistId,
            FinalSubmitRequest request
    ) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);

        if (p.getArtist() == null
                || p.getArtist().getId() == null
                || !p.getArtist().getId().equals(artistId)) {
            throw new BusinessRuleException(
                    "Only the assigned artist can final submit this performance"
            );
        }
        requireRole(artistId, festival.getId(), "ARTIST");

        if (festival.getState() != FestivalState.FINAL_SUBMISSION) {
            throw new BusinessRuleException(
                    "Final submit allowed only when festival is in FINAL_SUBMISSION state"
            );
        }

        if (p.getState() != PerformanceState.APPROVED) {
            throw new BusinessRuleException(
                    "Only APPROVED performances can be final submitted"
            ); // CHANGED
        }

        if (isBlank(p.getScheduledSlot())) {
            throw new BusinessRuleException(
                    "Performance must have a scheduledSlot before final submission"
            ); // CHANGED
        }

        if (request == null) {
            throw new BusinessRuleException("Final submit payload is required");
        }
        if (isBlank(request.getFinalSetlist())
                || isBlank(request.getFinalRehearsalTimes())
                || isBlank(request.getFinalTimeSlots())) {
            throw new BusinessRuleException(
                    "finalSetlist, finalRehearsalTimes and finalTimeSlots are required"
            );
        }

        p.setFinalSetlist(request.getFinalSetlist().trim());
        p.setFinalRehearsalTimes(request.getFinalRehearsalTimes().trim());
        p.setFinalTimeSlots(request.getFinalTimeSlots().trim());
        p.setFinalSubmittedAt(LocalDateTime.now());
        p.setState(PerformanceState.FINAL_SUBMITTED);

        return performanceRepository.save(p);
    }

    private PerformanceViewDto toViewDto(Performance p, Long userId) {
        Festival festival = requireFestival(p);

        boolean isVisitor = (userId == null);
        if (isVisitor) {
            if (festival.getState() != FestivalState.ANNOUNCED
                    && festival.getState() != FestivalState.COMPLETE) {
                return null;
            }
            if (p.getState() != PerformanceState.SCHEDULED) {
                return null;
            }

            PerformanceViewDto dto = new PerformanceViewDto();
            dto.setId(p.getId());
            dto.setFestivalId(festival.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setGenre(p.getGenre());
            dto.setScheduledSlot(p.getScheduledSlot());
            return dto;
        }

        PerformanceViewDto dto = new PerformanceViewDto();
        dto.setId(p.getId());
        dto.setFestivalId(festival.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setGenre(p.getGenre());
        dto.setScheduledSlot(p.getScheduledSlot());
        dto.setState(p.getState() == null ? null : p.getState().name());
        dto.setPreferredRehearsalTimes(p.getPreferredRehearsalTimes());
        dto.setPreferredTimeSlots(p.getPreferredTimeSlots());
        dto.setFinalSetlist(p.getFinalSetlist());
        dto.setFinalRehearsalTimes(p.getFinalRehearsalTimes());
        dto.setFinalTimeSlots(p.getFinalTimeSlots());

        reviewRepository
                .findTopByPerformanceIdOrderByIdDesc(p.getId())
                .ifPresent(r -> {
                    dto.setLastReviewScore(r.getScore());
                    dto.setLastReviewComments(r.getComments());
                });
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Performance> searchPerformancesAdvanced(
            Long festivalId,
            String q,
            String name,
            String genre,
            String bandMembers,
            String state,
            String scheduledFrom,
            String scheduledTo,
            String sortBy,
            String sortDir
    ) {
        List<Performance> all = performanceRepository.findByFestivalId(festivalId);

        List<String> tokens = tokenize(q);
        String nameL = safeLower(name);
        String genreL = safeLower(genre);
        String bandL = safeLower(bandMembers);

        Optional<PerformanceState> stateOpt;
        if (state != null && !state.isBlank()) {
            try {
                stateOpt = Optional.of(
                        PerformanceState.valueOf(state.trim().toUpperCase(Locale.ROOT))
                );
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException("Invalid state filter: " + state);
            }
        } else {
            stateOpt = Optional.empty();
        }

        Comparator<Performance> comparator = buildPerformanceComparator(
                sortBy,
                sortDir
        );
        LocalDateTime fromDt = parseFlexibleDateTime(scheduledFrom);
        LocalDateTime toDt = parseFlexibleDateTime(scheduledTo);

        return all
                .stream()
                .filter(p -> matchesAllTokens(p, tokens))
                .filter(p -> nameL.isEmpty() || safeLower(p.getName()).contains(nameL))
                .filter(p -> genreL.isEmpty() || safeLower(p.getGenre()).contains(genreL))
                .filter(
                        p -> bandL.isEmpty() || safeLower(p.getBandMembers()).contains(bandL)
                )
                .filter(p -> stateOpt.isEmpty() || p.getState() == stateOpt.get())
                .filter(p -> matchesScheduledRange(p, fromDt, toDt))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceViewDto> searchPerformancesView(
            Long festivalId,
            Long userId,
            String query
    ) {
        return searchPerformancesViewAdvanced(
                festivalId,
                userId,
                query,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceViewDto> searchPerformancesViewAdvanced(
            Long festivalId,
            Long userId,
            String q,
            String name,
            String genre,
            String bandMembers,
            String state,
            String scheduledFrom,
            String scheduledTo,
            String sortBy,
            String sortDir
    ) {
        return searchPerformancesAdvanced(
                festivalId,
                q,
                name,
                genre,
                bandMembers,
                state,
                scheduledFrom,
                scheduledTo,
                sortBy,
                sortDir
        )
                .stream()
                .map(p -> toViewDto(p, userId))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    private Comparator<Performance> buildPerformanceComparator(
            String sortBy,
            String sortDir
    ) {
        String key
                = sortBy == null ? "genre" : sortBy.trim().toLowerCase(Locale.ROOT);
        boolean desc = sortDir != null && sortDir.trim().equalsIgnoreCase("desc");

        Comparator<Performance> c;
        switch (key) {
            case "name" ->
                c = Comparator.comparing(
                        (Performance p) -> safeLower(p.getName()),
                        Comparator.nullsLast(String::compareTo)
                );
            case "state" ->
                c = Comparator.comparing(
                        (Performance p) -> p.getState() == null ? "" : p.getState().name(),
                        Comparator.nullsLast(String::compareTo)
                );
            case "scheduled" ->
                c = Comparator.comparing(
                        (Performance p) -> {
                            try {
                                return parseFlexibleDateTime(p.getScheduledSlot());
                            } catch (Exception e) {
                                return null;
                            }
                        },
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
            case "genre" ->
                c = Comparator.comparing(
                        (Performance p) -> safeLower(p.getGenre()),
                        Comparator.nullsLast(String::compareTo)
                ).thenComparing(
                        p -> safeLower(p.getName()),
                        Comparator.nullsLast(String::compareTo)
                );
            default ->
                c = Comparator.comparing(
                        (Performance p) -> safeLower(p.getGenre()),
                        Comparator.nullsLast(String::compareTo)
                ).thenComparing(
                        p -> safeLower(p.getName()),
                        Comparator.nullsLast(String::compareTo)
                );
        }

        return desc ? c.reversed() : c;
    }

    private List<String> tokenize(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return Arrays.stream(q.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    private boolean matchesAllTokens(Performance p, List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return true;
        }

        String haystack = (safeLower(p.getName())
                + " "
                + safeLower(p.getDescription())
                + " "
                + safeLower(p.getGenre())
                + " "
                + safeLower(p.getBandMembers())
                + " "
                + safeLower(p.getTechnicalRequirements())
                + " "
                + safeLower(p.getScheduledSlot()));

        for (String t : tokens) {
            if (!haystack.contains(t)) {
                return false;
            }
        }
        return true;
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private Festival requireFestival(Performance p) {
        if (p.getFestival() == null) {
            throw new BusinessRuleException(
                    "Performance has no festival"
            );
        }
        return p.getFestival();
    }

    private void ensureSubmitterRole(Long festivalId, Long userId) {
        if (festivalId == null || userId == null) {
            return;
        }

        Optional<String> existingRoleOpt
                = userFestivalRoleRepository.findRoleNameForUserInFestival(
                        userId,
                        festivalId
                );
        if (existingRoleOpt.isPresent()) {
            String existingRole = existingRoleOpt.get();
            if ("SUBMITTER".equalsIgnoreCase(existingRole)
                    || "ARTIST".equalsIgnoreCase(existingRole)) {
                return;
            }
            throw new BusinessRuleException(
                    "User already has role " + existingRole + " in this festival"
            );
        }

        Role role = roleRepository
                .findByName("SUBMITTER")
                .orElseGet(()
                        -> roleRepository
                        .findByName("ARTIST")
                        .orElseThrow(()
                                -> new BusinessRuleException("Role SUBMITTER/ARTIST not found")
                        )
                );

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        Festival festival = festivalRepository
                .findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        userFestivalRoleRepository.save(new UserFestivalRole(user, festival, role));
    }

    private void requireSubmitterOwner(Performance p, Long userId) {
        Festival festival = requireFestival(p);
        requireRole(userId, festival.getId(), "SUBMITTER");

        if (p.getArtist() == null || p.getArtist().getId() == null) {
            throw new BusinessRuleException("Performance has no artist owner");
        }
        if (userId == null || !p.getArtist().getId().equals(userId)) {
            throw new BusinessRuleException(
                    "Only the SUBMITTER/owner can perform this action"
            );
        }
    }

    private boolean hasRole(Long userId, Long festivalId, String roleName) {
        if (userId == null || festivalId == null || roleName == null) {
            return false;
        }
        Optional<String> roleOpt
                = userFestivalRoleRepository.findRoleNameForUserInFestival(
                        userId,
                        festivalId
                );
        if (roleOpt.isEmpty()) {
            return false;
        }
        String r = roleOpt.get();
        if (r.equalsIgnoreCase(roleName)) {
            return true;
        }

        if ("SUBMITTER".equalsIgnoreCase(roleName) && "ARTIST".equalsIgnoreCase(r)) {
            return true;
        }
        if ("ARTIST".equalsIgnoreCase(roleName) && "SUBMITTER".equalsIgnoreCase(r)) {
            return true;
        }

        return false;
    }

    private void requireRole(Long userId, Long festivalId, String roleName) {
        if (!hasRole(userId, festivalId, roleName)) {
            throw new BusinessRuleException("Missing required role: " + roleName);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Performance> searchPerformances(Long festivalId, String query) {
        List<Performance> all = performanceRepository.findByFestivalId(festivalId);

        List<String> tokens = tokenize(query);

        return all
                .stream()
                .filter(p -> matchesAllTokens(p, tokens))
                .sorted(
                        Comparator.comparing(
                                (Performance p) -> safeLower(p.getGenre()),
                                Comparator.nullsLast(String::compareTo)
                        ).thenComparing(
                                p -> safeLower(p.getName()),
                                Comparator.nullsLast(String::compareTo)
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Performance assignHandler(
            Long performanceId,
            Long programmerId,
            Long staffId
    ) {
        Performance performance = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = performance.getFestival();

        if (festival.getState() != FestivalState.ASSIGNMENT) {
            throw new BusinessRuleException(
                    "Handler assignment is allowed only in ASSIGNMENT state"
            );
        }

        if (!hasRole(programmerId, festival.getId(), "PROGRAMMER")) {
            throw new BusinessRuleException("Only PROGRAMMER can assign handlers");
        }

        if (!userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(
                staffId,
                festival.getId(),
                "STAFF"
        )) {
            throw new BusinessRuleException(
                    "Assigned handler must be STAFF in this festival"
            );
        }
        if (performance.getHandler() != null) {
            throw new BusinessRuleException(
                    "Handler is already assigned for this performance"
            );
        }

        User staff = userRepository
                .findById(staffId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        performance.setHandler(staff);
        return performanceRepository.save(performance);
    }

    private void ensureArtistRole(Long festivalId, Long userId) {
        if (userFestivalRoleRepository.existsByIdUserIdAndIdFestivalId(
                userId,
                festivalId
        )) {
            return;
        }

        Role artistRole = roleRepository
                .findByName("ARTIST")
                .orElseThrow(() -> new BusinessRuleException("Role ARTIST not found"));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        Festival festival = festivalRepository
                .findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        userFestivalRoleRepository.save(
                new UserFestivalRole(user, festival, artistRole)
        );
    }

    @Override
    @Transactional
    public Performance finalAccept(Long performanceId, Long programmerId) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = p.getFestival();

        if (festival.getState() != FestivalState.DECISION) {
            throw new BusinessRuleException(
                    "Final decisions are allowed only in DECISION state"
            );
        }

        if (!userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(
                programmerId,
                festival.getId(),
                "PROGRAMMER"
        )) {
            throw new BusinessRuleException(
                    "Only PROGRAMMER can make final decisions"
            );
        }

        if (p.getState() != PerformanceState.FINAL_SUBMITTED) {
            throw new BusinessRuleException(
                    "Only FINAL_SUBMITTED performances can be accepted"
            );
        }

        p.setState(PerformanceState.SCHEDULED);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance finalReject(
            Long performanceId,
            Long programmerId,
            String reason
    ) {
        Performance p = performanceRepository
                .findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = p.getFestival();

        if (festival.getState() != FestivalState.DECISION) {
            throw new BusinessRuleException(
                    "Final decisions are allowed only in DECISION state"
            );
        }

        if (!userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(
                programmerId,
                festival.getId(),
                "PROGRAMMER"
        )) {
            throw new BusinessRuleException(
                    "Only PROGRAMMER can make final decisions"
            );
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Rejection reason is required");
        }
        if (p.getState() != PerformanceState.FINAL_SUBMITTED) {
            throw new BusinessRuleException(
                    "Only FINAL_SUBMITTED performances can be rejected"
            );
        }

        User reviewer = userRepository
                .findById(programmerId)
                .orElseThrow(() -> new BusinessRuleException("Reviewer not found"));

        Review review = new Review();
        review.setPerformance(p);
        review.setReviewer(reviewer);
        review.setScore(0);
        review.setComments("FINAL REJECT: " + reason.trim());
        reviewRepository.save(review);

        p.setState(PerformanceState.REJECTED);
        p.setRejectionReason(reason.trim());
        performanceRepository.save(p);

        return p;
    }

    private LocalDateTime parseFlexibleDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();

        try {
            return LocalDateTime.parse(s);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(s + ":00");
        } catch (Exception ignored) {
        }

        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(s, f);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (Exception ignored) {
        }

        throw new BusinessRuleException("Invalid scheduledFrom/scheduledTo format: " + raw);
    }

    private boolean matchesScheduledRange(Performance p, LocalDateTime fromDt, LocalDateTime toDt) {
        if (fromDt == null && toDt == null) {
            return true;
        }

        LocalDateTime slot;
        try {
            slot = parseFlexibleDateTime(p.getScheduledSlot());
        } catch (Exception ex) {
            return false;
        }
        if (slot == null) {
            return false;
        }

        if (fromDt != null && slot.isBefore(fromDt)) {
            return false;
        }
        if (toDt != null && slot.isAfter(toDt)) {
            return false;
        }
        return true;
    }

}
