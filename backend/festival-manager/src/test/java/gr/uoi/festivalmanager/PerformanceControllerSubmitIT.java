package gr.uoi.festivalmanager;

import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.enums.PerformanceState;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import gr.uoi.festivalmanager.repository.PerformanceRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PerformanceControllerSubmitIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserRepository userRepository;
    @Autowired private FestivalRepository festivalRepository;
    @Autowired private PerformanceRepository performanceRepository;

    private User artist;
    private Festival festival;
    private Performance performance;

    @BeforeEach
    void setup() {
        performanceRepository.deleteAll();
        festivalRepository.deleteAll();
        userRepository.deleteAll();

        artist = new User();
        artist.setUsername("artist1");
        artist.setEmail("a1@test.com");
        artist.setPassword(passwordEncoder.encode("pass123"));
        artist.setEnabled(true);
        artist = userRepository.save(artist);

        festival = new Festival();
        festival.setTitle("Test Festival");
        festival.setStartDate(LocalDate.now());
        festival.setEndDate(LocalDate.now().plusDays(1));
        festival.setState(FestivalState.CREATED);
        festival = festivalRepository.save(festival);

        performance = new Performance();
        performance.setFestival(festival);
        performance.setArtist(artist);
        performance.setName("My Show");
        performance.setGenre("Rock");
        performance.setDurationMinutes(60);
        performance.setState(PerformanceState.CREATED);
        performance = performanceRepository.save(performance);
    }

    @Test
    void submit_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/performances/{id}/submit", performance.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submit_withAuth_butFestivalNotSubmission_returns400() throws Exception {

        mockMvc.perform(post("/api/performances/{id}/submit", performance.getId())
                        .with(httpBasic("artist1", "pass123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsStringIgnoringCase("submission")));
    }

    @Test
    void submit_withAuth_andFestivalInSubmission_returns200_andSubmitted() throws Exception {
        festival.setState(FestivalState.SUBMISSION);
        festivalRepository.save(festival);

        mockMvc.perform(post("/api/performances/{id}/submit", performance.getId())
                        .with(httpBasic("artist1", "pass123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("SUBMITTED"));
    }
}
