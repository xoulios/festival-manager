package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FestivalServiceImpl implements FestivalService {

    private final FestivalRepository festivalRepository;

    public FestivalServiceImpl(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    @Override
    public Festival createFestival(Festival festival) {
        festival.setState(FestivalState.CREATED);
        return festivalRepository.save(festival);
    }

    @Override
    public Festival updateFestival(Long festivalId, Festival updatedFestival) {
        Festival existing = getFestivalById(festivalId);

        if (existing.getState() != FestivalState.CREATED) {
            throw new IllegalStateException("Festival can be updated only in CREATED state");
        }

        existing.setTitle(updatedFestival.getTitle());
        existing.setDescription(updatedFestival.getDescription());
        existing.setStartDate(updatedFestival.getStartDate());
        existing.setEndDate(updatedFestival.getEndDate());

        return festivalRepository.save(existing);
    }

    @Override
    public void deleteFestival(Long festivalId) {
        Festival festival = getFestivalById(festivalId);

        if (festival.getState() != FestivalState.CREATED) {
            throw new IllegalStateException("Festival can be deleted only in CREATED state");
        }

        festivalRepository.delete(festival);
    }

    @Override
    public Festival changeState(Long festivalId, FestivalState newState) {
        Festival festival = getFestivalById(festivalId);

        if (!isValidTransition(festival.getState(), newState)) {
            throw new IllegalStateException(
                "Invalid state transition: " + festival.getState() + " -> " + newState
            );
        }

        festival.setState(newState);
        return festivalRepository.save(festival);
    }

    @Override
    public Festival getFestivalById(Long festivalId) {
        return festivalRepository.findById(festivalId)
            .orElseThrow(() -> new IllegalArgumentException("Festival not found"));
    }

    @Override
    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }

    private boolean isValidTransition(FestivalState current, FestivalState next) {
        return switch (current) {
            case CREATED -> next == FestivalState.SUBMISSION;
            case SUBMISSION -> next == FestivalState.ASSIGNMENT;
            case ASSIGNMENT -> next == FestivalState.REVIEW;
            case REVIEW -> next == FestivalState.SCHEDULING;
            case SCHEDULING -> next == FestivalState.FINAL_SUBMISSION;
            case FINAL_SUBMISSION -> next == FestivalState.DECISION;
            case DECISION -> next == FestivalState.ANNOUNCED;
            default -> false;
        };
    }
}
