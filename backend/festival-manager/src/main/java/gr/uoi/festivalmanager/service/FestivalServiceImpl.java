package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.AssignRoleRequest;
import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.dto.FestivalUpdateRequest;
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

import java.util.List;

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
        Festival festival = new Festival();
        festival.setTitle(request.getTitle());
        festival.setDescription(request.getDescription());
        festival.setStartDate(request.getStartDate());
        festival.setEndDate(request.getEndDate());
        festival.setState(FestivalState.CREATED);

        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public FestivalResponse updateFestival(Long id, FestivalUpdateRequest request) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        festival.setTitle(request.getTitle());
        festival.setDescription(request.getDescription());
        festival.setStartDate(request.getStartDate());
        festival.setEndDate(request.getEndDate());

        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
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
    public void deleteFestival(Long id) {
        if (!festivalRepository.existsById(id)) {
            throw new BusinessRuleException("Festival not found");
        }
        festivalRepository.deleteById(id);
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

        requireProgrammer(userId, festival.getId());

        validateTransition(festival.getState(), newState);

        festival.setState(newState);
        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
    }

    private void validateTransition(FestivalState current, FestivalState next) {
        if (current == null || next == null) {
            throw new BusinessRuleException("Invalid festival state");
        }
        if (current == next) {
            throw new BusinessRuleException("Festival is already in state " + next);
        }

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

        if (allowedNext == null) {
            throw new BusinessRuleException("Festival is in terminal state " + current);
        }
        if (next != allowedNext) {
            throw new BusinessRuleException("Invalid transition: " + current + " -> " + next);
        }
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
            requireProgrammer(actorId, festivalId);
        }

        if (festival.getState() != FestivalState.CREATED && festival.getState() != FestivalState.SUBMISSION) {
            throw new BusinessRuleException("Roles can be assigned only in CREATED or SUBMISSION state");
        }

        Long userId = request.getUserId();
        Long roleId = request.getRoleId();

        boolean alreadyHasRole = userFestivalRoleRepository.existsByIdUserIdAndIdFestivalId(userId, festivalId);
        if (alreadyHasRole) {
            throw new BusinessRuleException("User already has a role in this festival");
        }

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

        requireProgrammer(userId, festivalId);

        if (festival.getState() != FestivalState.FINAL_SUBMISSION) {
            throw new BusinessRuleException("DECISION can start only after FINAL_SUBMISSION");
        }

        festival.setState(FestivalState.DECISION);
        Festival savedFestival = festivalRepository.save(festival);

        List<Performance> performances = performanceRepository.findByFestivalId(festivalId);
        for (Performance p : performances) {
            if (p.getState() == PerformanceState.APPROVED) { 
                p.setState(PerformanceState.REJECTED); 
                performanceRepository.save(p); 
            }
        }

        return toResponse(savedFestival);
    }


    private void requireProgrammer(Long userId, Long festivalId) {
        if (userId == null) {
            throw new BusinessRuleException("userId is required");
        }

        boolean ok =
                userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(userId, festivalId, "PROGRAMMER")
                        || userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(userId, festivalId, "ORGANIZER");

        if (!ok) {
            throw new BusinessRuleException("Only PROGRAMMER can perform this action");
        }
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
