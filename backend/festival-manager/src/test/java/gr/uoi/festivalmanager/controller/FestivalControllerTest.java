package gr.uoi.festivalmanager.controller;

import gr.uoi.festivalmanager.service.FestivalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FestivalController.class)
@AutoConfigureMockMvc(addFilters = false)
class FestivalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FestivalService festivalService;

    @Test
    void assignRole_returnsOk() throws Exception {
        Long festivalId = 1L;
        Long userId = 2L;
        Long roleId = 3L;

        doNothing().when(festivalService).assignRole(festivalId, userId, roleId);

        mockMvc.perform(
                        post("/api/festivals/{festivalId}/assign-role", festivalId)
                                .param("userId", userId.toString())
                                .param("roleId", roleId.toString())
                )
                .andExpect(status().isOk());
    }
}
