package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.AssignRoleRequest;
import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.dto.FestivalUpdateRequest;
import gr.uoi.festivalmanager.dto.FestivalViewDto;
import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.entity.Role;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.enums.PerformanceState;
import gr.uoi.festivalmanager.exception.BusinessRuleException;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import gr.uoi.festivalmanager.repository.PerformanceRepository;
import gr.uoi.festivalmanager.repository.RoleRepository;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FestivalServiceImpl implements FestivalService {

    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserFestivalRoleRepository userFestivalRoleRepository;
    private final PerformanceRepository performanceRepository;

    public FestivalServiceImpl(
            FestivalRepository festivalRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserFestivalRoleRepository userFestivalRoleRepository,
            PerformanceRepository performanceRepository
    ) {
        this.festivalRepository = festivalRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userFestivalRoleRepository = userFestivalRoleRepository;
        this.performanceRepository = performanceRepository;
    }

    @Override
    @Transactional
    public FestivalResponse createFestival(FestivalCreateRequest request) {
        return createFestival(request, null);
    }

    @Override
    @Transactional
    public FestivalResponse createFestival(FestivalCreateRequest request, Long creatorUserId) {
        Festival festival = new Festival();
        festival.setDescription(request.getDescription());
        festival.setStartDate(request.getStartDate());
        festival.setEndDate(request.getEndDate());

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Start date must be before or equal to end date");
        }

        festival.setState(FestivalState.CREATED);

        String title = request.getTitle() == null ? null : request.getTitle().trim();
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Title is required");
        }
        if (festivalRepository.existsByTitleIgnoreCase(title)) {
            throw new BusinessRuleException("Festival title must be unique");
        }
        festival.setTitle(title);

        Festival saved = festivalRepository.save(festival);

        if (creatorUserId != null) {
            User creator = userRepository.findById(creatorUserId)
                    .orElseThrow(() -> new BusinessRuleException("User not found"));

            Role programmerRole = roleRepository.findByName("PROGRAMMER")
                    .orElseThrow(() -> new BusinessRuleException("Role PROGRAMMER not found"));

            boolean already = userFestivalRoleRepository.existsByIdUserIdAndIdFestivalId(creatorUserId, saved.getId());
            if (!already) {
                userFestivalRoleRepository.save(new UserFestivalRole(creator, saved, programmerRole));
            }
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public FestivalResponse updateFestival(Long id, Long actorUserId, FestivalUpdateRequest request) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        requireProgrammer(festival.getId(), actorUserId);

        if (festival.getState() == FestivalState.ANNOUNCED || festival.getState() == FestivalState.COMPLETE) {
            throw new BusinessRuleException("Festival cannot be updated after ANNOUNCED");
        }

        if (request.getTitle() != null) {
            String newTitle = request.getTitle().trim();
            if (newTitle.isBlank()) throw new BusinessRuleException("Title is required");

            boolean exists = festivalRepository.existsByTitleIgnoreCase(newTitle);
            if (exists && !newTitle.equalsIgnoreCase(festival.getTitle())) {
                throw new BusinessRuleException("Festival title must be unique");
            }
            festival.setTitle(newTitle);
        }

        if (request.getDescription() != null) {
            String desc = request.getDescription().trim();
            if (desc.isBlank()) throw new BusinessRuleException("Description is required");
            festival.setDescription(desc);
        }

        if (request.getStartDate() != null) festival.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) festival.setEndDate(request.getEndDate());

        if (festival.getStartDate() != null && festival.getEndDate() != null
                && festival.getStartDate().isAfter(festival.getEndDate())) {
            throw new BusinessRuleException("Start date must be before or equal to end date");
        }

        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FestivalViewDto> searchView(
            Long viewerUserId,
            String title,
            String description,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            String performanceTitle
    ) {
        List<Festival> all = festivalRepository.findAll();

        String t = (title == null) ? null : title.trim().toLowerCase(Locale.ROOT);
        String d = (description == null) ? null : description.trim().toLowerCase(Locale.ROOT);
        String pt = (performanceTitle == null) ? null : performanceTitle.trim().toLowerCase(Locale.ROOT);

        List<Festival> filtered = all.stream()
                .filter(f -> t == null || (f.getTitle() != null && f.getTitle().toLowerCase(Locale.ROOT).contains(t)))
                .filter(f -> d == null || (f.getDescription() != null && f.getDescription().toLowerCase(Locale.ROOT).contains(d)))
                .filter(f -> startDateFrom == null || (f.getStartDate() != null && !f.getStartDate().isBefore(startDateFrom)))
                .filter(f -> startDateTo == null || (f.getStartDate() != null && !f.getStartDate().isAfter(startDateTo)))
                .filter(f -> endDateFrom == null || (f.getEndDate() != null && !f.getEndDate().isBefore(endDateFrom)))
                .filter(f -> endDateTo == null || (f.getEndDate() != null && !f.getEndDate().isAfter(endDateTo)))
                .filter(f -> {
                    if (pt == null) return true;
                    List<Performance> perfs = performanceRepository.findByFestivalId(f.getId());
                    return perfs.stream().anyMatch(p ->
                            p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(pt)
                    );
                })
                .collect(Collectors.toList());

        return filtered.stream()
                .filter(f -> canViewFestival(viewerUserId, f))
                .sorted(Comparator.comparing(Festival::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Festival::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(f -> toViewDto(viewerUserId, f))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FestivalViewDto viewFestival(Long viewerUserId, Long festivalId) {
        Festival f = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        if (!canViewFestival(viewerUserId, f)) {
            throw new BusinessRuleException("Festival not found");
        }
        return toViewDto(viewerUserId, f);
    }

    private boolean canViewFestival(Long viewerUserId, Festival f) {
        if (viewerUserId == null) {
            return f.getState() == FestivalState.ANNOUNCED;
        }
        boolean hasRole = userFestivalRoleRepository.existsByIdUserIdAndIdFestivalId(viewerUserId, f.getId());
        return hasRole || f.getState() == FestivalState.ANNOUNCED;
    }

    private FestivalViewDto toViewDto(Long viewerUserId, Festival f) {
        String state = redactStateIfNeeded(viewerUserId, f);
        return new FestivalViewDto(
                f.getId(),
                f.getTitle(),
                f.getDescription(),
                f.getStartDate(),
                f.getEndDate(),
                state,
                userFestivalRoleRepository.findProgrammerUsernames(f.getId())
        );
    }

    private String redactStateIfNeeded(Long viewerUserId, Festival f) {
        if (f.getState() == null) return null;

        if (viewerUserId == null) {
            return (f.getState() == FestivalState.ANNOUNCED) ? f.getState().name() : null;
        }

        String roleName = userFestivalRoleRepository
                .findRoleNameForUserInFestival(viewerUserId, f.getId())
                .orElse(null);

        boolean privileged = "PROGRAMMER".equals(roleName) || "ORGANIZER".equals(roleName);

        if (privileged) return f.getState().name();
        if (f.getState() == FestivalState.ANNOUNCED) return f.getState().name();
        return null;
    }

    @Override
    @Transactional
    public void deleteFestival(Long id, Long actorUserId) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        requireProgrammer(id, actorUserId);

        if (festival.getState() != FestivalState.CREATED) {
            throw new BusinessRuleException("Festival can be deleted only in CREATED state");
        }

        festivalRepository.delete(festival);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FestivalResponse> listFestivals() {
        return festivalRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FestivalResponse getFestival(Long id) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));
        return toResponse(festival);
    }

    @Override
    @Transactional
    public FestivalResponse changeState(Long id, FestivalState newState) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        validateTransition(festival.getState(), newState);

        festival.setState(newState);
        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public FestivalResponse changeState(Long id, Long userId, FestivalState newState) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        requireProgrammer(festival.getId(), userId);

        validateTransition(festival.getState(), newState);

        festival.setState(newState);
        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
    }

    private void validateTransition(FestivalState current, FestivalState next) {
        if (current == null || next == null) throw new BusinessRuleException("Invalid festival state");
        if (current == next) throw new BusinessRuleException("Festival is already in state " + next);

        FestivalState allowedNext = switch (current) {
            case CREATED -> FestivalState.SUBMISSION;
            case SUBMISSION -> FestivalState.ASSIGNMENT;
            case ASSIGNMENT -> FestivalState.REVIEW;
            case REVIEW -> FestivalState.SCHEDULING;
            case SCHEDULING -> FestivalState.FINAL_SUBMISSION;
            case FINAL_SUBMISSION -> FestivalState.DECISION;
            case DECISION -> FestivalState.ANNOUNCED;
            case ANNOUNCED -> FestivalState.COMPLETE;
            case COMPLETE -> null;
        };

        if (allowedNext == null) throw new BusinessRuleException("Festival is in terminal state " + current);
        if (next != allowedNext) throw new BusinessRuleException("Invalid transition: " + current + " -> " + next);
    }

    private void requireProgrammer(Long festivalId, Long userId) {
        if (userId == null) throw new BusinessRuleException("Unauthenticated user");

        boolean ok = userFestivalRoleRepository
                .existsByIdUserIdAndIdFestivalIdAndRole_Name(userId, festivalId, "PROGRAMMER");

        if (!ok) throw new BusinessRuleException("Only PROGRAMMER can perform this action");
    }

    @Override
    @Transactional
    public void assignRole(Long festivalId, AssignRoleRequest request) {
        internalAssignRole(festivalId, null, request);
    }

    @Override
    @Transactional
    public void assignRole(Long festivalId, Long actorId, AssignRoleRequest request) {
        internalAssignRole(festivalId, actorId, request);
    }

    @Override
    @Transactional
    public void assignRole(Long festivalId, Long userId, Long roleId) {
        internalAssignRole(festivalId, null, new AssignRoleRequest(userId, roleId));
    }

    @Override
    @Transactional
    public void assignRole(Long organizerId, Long festivalId, Long userId, Long roleId) {
        internalAssignRole(festivalId, organizerId, new AssignRoleRequest(userId, roleId));
    }

    private void internalAssignRole(Long festivalId, Long actorId, AssignRoleRequest request) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        if (actorId != null) {
            requireProgrammer(festivalId, actorId);
        }

        if (festival.getState() != FestivalState.CREATED && festival.getState() != FestivalState.SUBMISSION) {
            throw new BusinessRuleException("Roles can be assigned only in CREATED or SUBMISSION state");
        }

        Long userId = request.getUserId();
        Long roleId = request.getRoleId();

        boolean alreadyHasRole = userFestivalRoleRepository.existsByIdUserIdAndIdFestivalId(userId, festivalId);
        if (alreadyHasRole) throw new BusinessRuleException("User already has a role in this festival");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessRuleException("Role not found"));

        userFestivalRoleRepository.save(new UserFestivalRole(user, festival, role));
    }

    @Override
    @Transactional
    public FestivalResponse moveToDecision(Long festivalId, Long userId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        requireProgrammer(festivalId, userId);

        if (festival.getState() != FestivalState.FINAL_SUBMISSION) {
            throw new BusinessRuleException("DECISION can start only after FINAL_SUBMISSION");
        }

        festival.setState(FestivalState.DECISION);
        Festival savedFestival = festivalRepository.save(festival);

        List<Performance> performances = performanceRepository.findByFestivalId(festivalId);
        for (Performance p : performances) {
            if (p.getState() == PerformanceState.APPROVED) {
                p.setState(PerformanceState.REJECTED);
                p.setRejectionReason("AUTO-REJECT: not finally submitted");
                performanceRepository.save(p);
            }
        }

        return toResponse(savedFestival);
    }

    private FestivalResponse toResponse(Festival f) {
        return new FestivalResponse(
                f.getId(),
                f.getTitle(),
                f.getDescription(),
                f.getStartDate(),
                f.getEndDate(),
                f.getState() == null ? null : f.getState().name()
        );
    }
}
