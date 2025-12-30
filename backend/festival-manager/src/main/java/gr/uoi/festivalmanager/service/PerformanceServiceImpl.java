package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.ReviewRequest;
import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.entity.Review;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.enums.PerformanceState;
import gr.uoi.festivalmanager.exception.BusinessRuleException;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import gr.uoi.festivalmanager.repository.PerformanceRepository;
import gr.uoi.festivalmanager.repository.ReviewRepository;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gr.uoi.festivalmanager.dto.PerformanceViewDto;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final UserFestivalRoleRepository userFestivalRoleRepository;
    private final ReviewRepository reviewRepository;

    public PerformanceServiceImpl(
            PerformanceRepository performanceRepository,
            FestivalRepository festivalRepository,
            UserRepository userRepository,
            UserFestivalRoleRepository userFestivalRoleRepository,
            ReviewRepository reviewRepository
    ) {
        this.performanceRepository = performanceRepository;
        this.festivalRepository = festivalRepository;
        this.userRepository = userRepository;
        this.userFestivalRoleRepository = userFestivalRoleRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public Performance createPerformance(Long festivalId, Long artistId, Performance performance) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessRuleException("Artist not found"));

        performance.setFestival(festival);
        performance.setArtist(artist);

        if (performance.getState() == null) {
            performance.setState(PerformanceState.CREATED);
        }

        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public Performance updatePerformance(Long performanceId, Performance updated) {
        Performance existing = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        if (existing.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException("Performance can be updated only in CREATED state");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setGenre(updated.getGenre());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setBandMembers(updated.getBandMembers());
        existing.setTechnicalRequirements(updated.getTechnicalRequirements());
        existing.setSetlist(updated.getSetlist());
        existing.setPreferredRehearsalTimes(updated.getPreferredRehearsalTimes());
        existing.setPreferredTimeSlots(updated.getPreferredTimeSlots());

        return performanceRepository.save(existing);
    }

    @Override
    @Transactional
    public Performance submitPerformance(Long performanceId) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        if (p.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException("Only CREATED performances can be submitted");
        }

        Festival festival = requireFestival(p);
        if (festival.getState() != FestivalState.SUBMISSION) {
            throw new BusinessRuleException("Submission allowed only when festival is in SUBMISSION state");
        }

        p.setState(PerformanceState.SUBMITTED);
        p.setSubmittedAt(LocalDateTime.now());

        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance withdrawPerformance(Long performanceId) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        if (p.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException("Withdraw allowed only in CREATED state");
        }

        p.setState(PerformanceState.WITHDRAWN);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance reviewPerformance(Long performanceId, Long staffId, ReviewRequest request) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);
        requireRole(staffId, festival.getId(), "STAFF");

        if (festival.getState() != FestivalState.REVIEW) {
            throw new BusinessRuleException("Review allowed only when festival is in REVIEW state");
        }
        if (p.getState() != PerformanceState.SUBMITTED) {
            throw new BusinessRuleException("Only SUBMITTED performances can be reviewed");
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

        User reviewer = userRepository.findById(staffId)
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
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);
        requireRole(staffId, festival.getId(), "STAFF");

        if (festival.getState() != FestivalState.REVIEW) {
            throw new BusinessRuleException("Approval allowed only when festival is in REVIEW state");
        }
        if (p.getState() != PerformanceState.REVIEWED) {
            throw new BusinessRuleException("Only REVIEWED performances can be approved");
        }

        p.setState(PerformanceState.APPROVED);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance rejectPerformance(Long performanceId, Long staffId, String reason) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);
        requireRole(staffId, festival.getId(), "STAFF");

        if (festival.getState() != FestivalState.REVIEW) {
            throw new BusinessRuleException("Rejection allowed only when festival is in REVIEW state");
        }
        if (p.getState() != PerformanceState.REVIEWED) {
            throw new BusinessRuleException("Only REVIEWED performances can be rejected");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Rejection reason is required");
        }

        User reviewer = userRepository.findById(staffId)
                .orElseThrow(() -> new BusinessRuleException("Reviewer not found"));

        Review review = new Review();
        review.setPerformance(p);
        review.setReviewer(reviewer);
        review.setScore(0);
        review.setComments("REJECT: " + reason.trim());
        reviewRepository.save(review);

        p.setState(PerformanceState.REJECTED);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance schedulePerformance(Long performanceId, Long schedulerId, String scheduledSlot) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);

        boolean isOrganizer = hasRole(schedulerId, festival.getId(), "ORGANIZER");
        boolean isStaff = hasRole(schedulerId, festival.getId(), "STAFF");
        if (!isOrganizer && !isStaff) {
            throw new BusinessRuleException("Only ORGANIZER or STAFF can schedule performances");
        }

        if (festival.getState() != FestivalState.SCHEDULING) {
            throw new BusinessRuleException("Scheduling allowed only when festival is in SCHEDULING state");
        }
        if (p.getState() != PerformanceState.APPROVED) {
            throw new BusinessRuleException("Only APPROVED performances can be scheduled");
        }
        if (scheduledSlot == null || scheduledSlot.trim().isEmpty()) {
            throw new BusinessRuleException("scheduledSlot is required");
        }

        p.setScheduledSlot(scheduledSlot.trim());
        p.setState(PerformanceState.SCHEDULED);
        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance finalSubmitPerformance(Long performanceId, Long artistId, FinalSubmitRequest request) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = requireFestival(p);

        if (p.getArtist() == null || p.getArtist().getId() == null || !p.getArtist().getId().equals(artistId)) {
            throw new BusinessRuleException("Only the assigned artist can final submit this performance");
        }
        requireRole(artistId, festival.getId(), "ARTIST");

        if (festival.getState() != FestivalState.FINAL_SUBMISSION) {
            throw new BusinessRuleException("Final submit allowed only when festival is in FINAL_SUBMISSION state");
        }
        if (p.getState() != PerformanceState.SCHEDULED) {
            throw new BusinessRuleException("Only SCHEDULED performances can be final submitted");
        }
        if (request == null) {
            throw new BusinessRuleException("Final submit payload is required");
        }
        if (isBlank(request.getFinalSetlist()) || isBlank(request.getFinalRehearsalTimes()) || isBlank(request.getFinalTimeSlots())) {
            throw new BusinessRuleException("finalSetlist, finalRehearsalTimes and finalTimeSlots are required");
        }

        p.setFinalSetlist(request.getFinalSetlist().trim());
        p.setFinalRehearsalTimes(request.getFinalRehearsalTimes().trim());
        p.setFinalTimeSlots(request.getFinalTimeSlots().trim());
        p.setFinalSubmittedAt(LocalDateTime.now());
        p.setState(PerformanceState.FINAL_SUBMITTED);

        return performanceRepository.save(p);
    }

        @Override
    @Transactional(readOnly = true)
    public List<PerformanceViewDto> searchPerformancesView(Long festivalId, Long userId, String query) {

        List<Performance> matches = searchPerformances(festivalId, query);

        return matches.stream()
                .map(p -> toViewDto(p, userId))
                .filter(dto -> dto != null)
                .toList();
    }

    private PerformanceViewDto toViewDto(Performance p, Long userId) {
        Long festivalId = p.getFestival().getId();
        boolean isProgrammer = hasRole(userId, festivalId, "PROGRAMMER") || hasRole(userId, festivalId, "ORGANIZER");
        boolean isStaff = hasRole(userId, festivalId, "STAFF");
        boolean isArtist = hasRole(userId, festivalId, "ARTIST") && p.getArtist() != null && userId.equals(p.getArtist().getId());
        boolean isAssignedStaff = isStaff && p.getHandler() != null && p.getHandler().getId() != null && p.getHandler().getId().equals(userId);
        boolean isVisitor = !isProgrammer && !isStaff && !isArtist;

        if (isVisitor) {
            switch (p.getState()) {
                case SCHEDULED, FINAL_SUBMITTED -> { /* ok */ }
                default -> { return null; }
            }
        }

        if (isStaff && !isProgrammer && !isAssignedStaff) {
            return null;
        }

        PerformanceViewDto dto = new PerformanceViewDto();
        dto.setId(p.getId());
        dto.setFestivalId(festivalId);
        dto.setName(p.getName());
        dto.setGenre(p.getGenre());
        dto.setDescription(p.getDescription());

        if (!isVisitor) {
            dto.setState(p.getState() == null ? null : p.getState().name());
        } else {
            dto.setState(p.getState() == null ? null : p.getState().name());
        }

        if (isProgrammer || isAssignedStaff || isArtist) {
            dto.setScheduledSlot(p.getScheduledSlot());
        }

        if (isProgrammer || isArtist || isAssignedStaff) {
            dto.setPreferredRehearsalTimes(p.getPreferredRehearsalTimes());
            dto.setPreferredTimeSlots(p.getPreferredTimeSlots());
        }

        if (isProgrammer || isAssignedStaff || isArtist) {
            dto.setFinalSetlist(p.getFinalSetlist());
            dto.setFinalRehearsalTimes(p.getFinalRehearsalTimes());
            dto.setFinalTimeSlots(p.getFinalTimeSlots());
        }

        if (isProgrammer || isAssignedStaff) {
            Optional<Review> last = reviewRepository.findTopByPerformanceIdOrderByIdDesc(p.getId());
            if (last.isPresent()) {
                dto.setLastReviewScore(last.get().getScore());
                dto.setLastReviewComments(last.get().getComments());
            }
        }

        return dto;
    }


    private Festival requireFestival(Performance p) {
        Festival f = p.getFestival();
        if (f == null || f.getId() == null) {
            throw new BusinessRuleException("Performance has no festival");
        }
        return f;
    }

    private void requireRole(Long userId, Long festivalId, String roleName) {
        if (!hasRole(userId, festivalId, roleName)) {
            throw new BusinessRuleException("User does not have required role: " + roleName);
        }
    }

    private boolean hasRole(Long userId, Long festivalId, String roleName) {
        return userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(userId, festivalId, roleName);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

        @Override
    @Transactional(readOnly = true)
    public List<Performance> searchPerformances(Long festivalId, String query) {
        List<Performance> all = performanceRepository.findByFestivalId(festivalId);

        List<String> tokens = tokenize(query);

        return all.stream()
                .filter(p -> matchesAllTokens(p, tokens))
                .sorted(
                        Comparator
                                .comparing((Performance p) -> safeLower(p.getGenre()), Comparator.nullsLast(String::compareTo))
                                .thenComparing(p -> safeLower(p.getName()), Comparator.nullsLast(String::compareTo))
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Performance assignHandler(Long performanceId, Long programmerId, Long staffId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        Festival festival = performance.getFestival();

        if (festival.getState() != FestivalState.ASSIGNMENT) {
            throw new BusinessRuleException("Handler assignment is allowed only in ASSIGNMENT state");
        }

        if (!userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(programmerId, festival.getId(), "PROGRAMMER")) {
            throw new BusinessRuleException("Only PROGRAMMER can assign handlers");
        }

        if (!userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(staffId, festival.getId(), "STAFF")) {
            throw new BusinessRuleException("Assigned handler must be STAFF in this festival");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        performance.setHandler(staff);
        return performanceRepository.save(performance);
    }


    private List<String> tokenize(String query) {
        if (query == null || query.trim().isEmpty()) return List.of();

        return Arrays.stream(query.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();
    }

    private boolean matchesAllTokens(Performance p, List<String> tokens) {
        if (tokens.isEmpty()) return true;

        String haystack = (
                safeLower(p.getName()) + " " +
                safeLower(p.getDescription()) + " " +
                safeLower(p.getGenre())
        );

        for (String t : tokens) {
            if (!haystack.contains(t)) return false;
        }
        return true;
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

}
