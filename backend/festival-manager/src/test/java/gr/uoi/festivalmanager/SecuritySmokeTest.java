package gr.uoi.festivalmanager;

import gr.uoi.festivalmanager.entity.User;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecuritySmokeTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        if (userRepository.findByUsername("programmer1").isEmpty()) {
            User u = new User();
            u.setUsername("programmer1");
            u.setEmail("p1@test.com");
            u.setPassword(passwordEncoder.encode("pass123"));
            u.setEnabled(true);
            userRepository.save(u);
        }
    }

    @Test
    void performancesSearchView_shouldBePublic() throws Exception {
        mockMvc.perform(get("/api/performances/search-view")
                .param("festivalId", "1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void festivalsList_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/festivals"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void festivalsList_withBasicAuth_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/festivals")
                .with(httpBasic("programmer1", "pass123")))
            .andExpect(status().isOk());
    }
}
