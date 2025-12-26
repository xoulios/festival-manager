package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.enums.FestivalState;
import java.util.List;

public interface FestivalService {
    Festival createFestival(Festival festival);
    Festival updateFestival(Long id, Festival festival);
    void deleteFestival(Long id);

    Festival changeState(Long id, FestivalState newState);
    FestivalState getState(Long id);

    List<Festival> getAllFestivals();

    void assignRole(Long festivalId, Long userId, Long roleId);
}
