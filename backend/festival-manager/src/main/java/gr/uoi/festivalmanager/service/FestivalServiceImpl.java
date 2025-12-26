package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.entity.Role;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.exception.BusinessRuleException;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import gr.uoi.festivalmanager.repository.RoleRepository;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FestivalServiceImpl implements FestivalService {

    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserFestivalRoleRepository userFestivalRoleRepository;

    public FestivalServiceImpl(
            FestivalRepository festivalRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserFestivalRoleRepository userFestivalRoleRepository
    ) {
        this.festivalRepository = festivalRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userFestivalRoleRepository = userFestivalRoleRepository;
    }

    @Override
    public Festival createFestival(Festival festival) {
        festival.setState(FestivalState.CREATED);
        return festivalRepository.save(festival);
    }

    @Override
    public Festival updateFestival(Long id, Festival festival) {
        Festival existing = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        if (existing.getState() != FestivalState.CREATED) {
            throw new BusinessRuleException("Festival can only be updated in CREATED state");
        }

        existing.setTitle(festival.getTitle());
        existing.setDescription(festival.getDescription());
        existing.setStartDate(festival.getStartDate());
        existing.setEndDate(festival.getEndDate());

        return festivalRepository.save(existing);
    }

    @Override
    public void deleteFestival(Long id) {
        Festival existing = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        if (existing.getState() != FestivalState.CREATED) {
            throw new BusinessRuleException("Festival can only be deleted in CREATED state");
        }

        festivalRepository.delete(existing);
    }

    @Override
    public Festival changeState(Long id, FestivalState newState) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        FestivalState current = festival.getState();

        if (!isValidTransition(current, newState)) {
            throw new BusinessRuleException("Invalid festival state transition: " + current + " -> " + newState);
        }

        festival.setState(newState);
        return festivalRepository.save(festival);
    }

    @Override
    public FestivalState getState(Long id) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));
        return festival.getState();
    }

    @Override
    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }

    @Override
    public void assignRole(Long festivalId, Long userId, Long roleId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        if (festival.getState() != FestivalState.ASSIGNMENT) {
            throw new BusinessRuleException("Roles can only be assigned in ASSIGNMENT state");
        }

        if (userFestivalRoleRepository.existsByIdUserIdAndIdFestivalId(userId, festivalId)) {
            throw new BusinessRuleException("User already has a role in this festival");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessRuleException("Role not found"));

        UserFestivalRole userFestivalRole = new UserFestivalRole(user, festival, role);
        userFestivalRoleRepository.save(userFestivalRole);
    }

    private boolean isValidTransition(FestivalState current, FestivalState next) {
        if (current == next) return true;

        return switch (current) {
            case CREATED -> next == FestivalState.SUBMISSION;
            case SUBMISSION -> next == FestivalState.ASSIGNMENT;
            case ASSIGNMENT -> next == FestivalState.REVIEW;
            case REVIEW -> next == FestivalState.SCHEDULING;
            case SCHEDULING -> next == FestivalState.FINAL_SUBMISSION;
            case FINAL_SUBMISSION -> next == FestivalState.DECISION;
            case DECISION -> next == FestivalState.ANNOUNCED;
            case ANNOUNCED -> false;
        };
    }
}
