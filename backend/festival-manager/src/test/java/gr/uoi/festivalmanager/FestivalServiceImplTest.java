package gr.uoi.festivalmanager;

import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.entity.Role;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.exception.BusinessRuleException;
import gr.uoi.festivalmanager.repository.RoleRepository;
import gr.uoi.festivalmanager.repository.UserFestivalRoleRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import gr.uoi.festivalmanager.service.FestivalService;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FestivalServiceImplTest {

    @Autowired private FestivalService festivalService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserFestivalRoleRepository userFestivalRoleRepository;

    private Long programmerId;

    @BeforeEach
    void setup() {
        Role programmerRole = new Role();
        programmerRole.setName("PROGRAMMER");
        roleRepository.save(programmerRole);

        User programmer = new User();
        programmer.setUsername("programmer1");
        programmer.setEmail("p1@test.com");
        programmer.setPassword("{noop}pass123");
        programmer.setEnabled(true);
        programmer = userRepository.save(programmer);

        programmerId = programmer.getId();
    }

    @Test
    void createFestival_assignsProgrammerRole_and_stateCreated() {
        FestivalCreateRequest req = new FestivalCreateRequest();
        req.setTitle("  Demo Festival  ");
        req.setDescription("desc");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(1));

        FestivalResponse created = festivalService.createFestival(req, programmerId);

        assertNotNull(created.getId());
        assertEquals("Demo Festival", created.getTitle());
        assertEquals("CREATED", created.getState());

        boolean hasProgrammerRole =
            userFestivalRoleRepository.existsByIdUserIdAndIdFestivalIdAndRole_Name(
                programmerId, created.getId(), "PROGRAMMER"
            );

        assertTrue(hasProgrammerRole, "Creator should auto-get PROGRAMMER role on festival");
    }

    @Test
    void createFestival_uniqueTitle_enforced() {
        FestivalCreateRequest req = new FestivalCreateRequest();
        req.setTitle("Same Title");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(1));

        festivalService.createFestival(req, programmerId);

        BusinessRuleException ex = assertThrows(
            BusinessRuleException.class,
            () -> festivalService.createFestival(req, programmerId)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unique"));
    }

    @Test
    void changeState_validTransition_works() {
        FestivalCreateRequest req = new FestivalCreateRequest();
        req.setTitle("State Festival");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(1));

        FestivalResponse created = festivalService.createFestival(req, programmerId);

        FestivalResponse moved = festivalService.changeState(created.getId(), programmerId, FestivalState.SUBMISSION);
        assertEquals("SUBMISSION", moved.getState());
    }

    @Test
    void changeState_invalidTransition_throws() {
        FestivalCreateRequest req = new FestivalCreateRequest();
        req.setTitle("Invalid Transition Festival");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(1));

        FestivalResponse created = festivalService.createFestival(req, programmerId);

        assertThrows(BusinessRuleException.class, () ->
            festivalService.changeState(created.getId(), programmerId, FestivalState.REVIEW)
        );
    }
}
