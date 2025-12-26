package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.AssignRoleRequest;
import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.dto.FestivalUpdateRequest;
import gr.uoi.festivalmanager.enums.FestivalState;

import java.util.List;

public interface FestivalService {

    FestivalResponse createFestival(FestivalCreateRequest request);

    FestivalResponse updateFestival(Long id, FestivalUpdateRequest request);

    List<FestivalResponse> listFestivals();

    FestivalResponse getFestival(Long id);

    void deleteFestival(Long id);

    FestivalResponse changeState(Long id, FestivalState newState);

    void assignRole(Long festivalId, AssignRoleRequest request);
    void assignRole(Long festivalId, Long userId, Long roleId);
    void assignRole(Long organizerId, Long festivalId, Long userId, Long roleId);
}
