package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.AssignRoleRequest;
import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.dto.FestivalUpdateRequest;
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

        festival.setState(newState);

        Festival saved = festivalRepository.save(festival);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void assignRole(Long festivalId, Long userId, Long roleId) {
    assignRole(festivalId, new AssignRoleRequest(userId, roleId));
    }

    
    @Override
    @Transactional
    public void assignRole(Long festivalId, AssignRoleRequest request) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        if (festival.getState() != FestivalState.ASSIGNMENT) {
            throw new BusinessRuleException("Roles can be assigned only in ASSIGNMENT state");
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
    public void assignRole(Long organizerId, Long festivalId, Long userId, Long roleId) {
        assignRole(festivalId, new AssignRoleRequest(userId, roleId));
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
