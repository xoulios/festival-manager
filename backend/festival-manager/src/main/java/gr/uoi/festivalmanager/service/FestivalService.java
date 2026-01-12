package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.dto.AssignRoleRequest;
import gr.uoi.festivalmanager.dto.FestivalCreateRequest;
import gr.uoi.festivalmanager.dto.FestivalResponse;
import gr.uoi.festivalmanager.dto.FestivalUpdateRequest;
import gr.uoi.festivalmanager.dto.FestivalViewDto;
import gr.uoi.festivalmanager.enums.FestivalState;

import java.time.LocalDate;
import java.util.List;

public interface FestivalService {

    FestivalResponse createFestival(FestivalCreateRequest request);
    FestivalResponse createFestival(FestivalCreateRequest request, Long creatorUserId);

    FestivalResponse updateFestival(Long id, Long actorUserId, FestivalUpdateRequest request);

    List<FestivalResponse> listFestivals();
    FestivalResponse getFestival(Long id);

    void deleteFestival(Long id, Long actorUserId);

    FestivalResponse changeState(Long id, FestivalState newState);
    FestivalResponse changeState(Long id, Long userId, FestivalState newState);

    FestivalResponse moveToDecision(Long festivalId, Long userId);

    void assignRole(Long festivalId, AssignRoleRequest request);
    void assignRole(Long festivalId, Long actorId, AssignRoleRequest request);
    void assignRole(Long festivalId, Long userId, Long roleId);
    void assignRole(Long organizerId, Long festivalId, Long userId, Long roleId);

    List<FestivalViewDto> searchView(
            Long viewerUserId,
            String title,
            String description,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            String performanceTitle
    );

    FestivalViewDto viewFestival(Long viewerUserId, Long festivalId);
}
