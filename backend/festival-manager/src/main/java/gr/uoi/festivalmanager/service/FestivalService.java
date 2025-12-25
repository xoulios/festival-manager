package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.enums.FestivalState;

import java.util.List;

public interface FestivalService {

    Festival createFestival(Festival festival);

    Festival updateFestival(Long festivalId, Festival updatedFestival);

    void deleteFestival(Long festivalId);

    Festival changeState(Long festivalId, FestivalState newState);

    Festival getFestivalById(Long festivalId);

    List<Festival> getAllFestivals();
}
