package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Festival;
import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.entity.User;
import gr.uoi.festivalmanager.enums.FestivalState;
import gr.uoi.festivalmanager.enums.PerformanceState;
import gr.uoi.festivalmanager.exception.BusinessRuleException;
import gr.uoi.festivalmanager.repository.FestivalRepository;
import gr.uoi.festivalmanager.repository.PerformanceRepository;
import gr.uoi.festivalmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;

    public PerformanceServiceImpl(
            PerformanceRepository performanceRepository,
            FestivalRepository festivalRepository,
            UserRepository userRepository
    ) {
        this.performanceRepository = performanceRepository;
        this.festivalRepository = festivalRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Performance createPerformance(Long festivalId, Long artistId, Performance performance) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new BusinessRuleException("Festival not found"));

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessRuleException("Artist not found"));

        performance.setFestival(festival);
        performance.setArtist(artist);

        if (performance.getState() == null) {
            performance.setState(PerformanceState.CREATED);
        }

        return performanceRepository.save(performance);
    }

    @Override
    @Transactional
    public Performance updatePerformance(Long performanceId, Performance updated) {
        Performance existing = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        if (existing.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException("Performance can be updated only in CREATED state");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setGenre(updated.getGenre());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setBandMembers(updated.getBandMembers());
        existing.setTechnicalRequirements(updated.getTechnicalRequirements());
        existing.setSetlist(updated.getSetlist());
        existing.setPreferredRehearsalTimes(updated.getPreferredRehearsalTimes());
        existing.setPreferredTimeSlots(updated.getPreferredTimeSlots());

        return performanceRepository.save(existing);
    }

    @Override
    @Transactional
    public Performance submitPerformance(Long performanceId) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        if (p.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException("Only CREATED performances can be submitted");
        }

        Festival festival = p.getFestival();
        if (festival == null) {
            throw new BusinessRuleException("Performance has no festival");
        }

        if (festival.getState() != FestivalState.SUBMISSION) {
            throw new BusinessRuleException("Performance submission allowed only when festival is in SUBMISSION state");
        }

        p.setState(PerformanceState.SUBMITTED);
        p.setSubmittedAt(java.time.LocalDateTime.now());

        return performanceRepository.save(p);
    }

    @Override
    @Transactional
    public Performance withdrawPerformance(Long performanceId) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessRuleException("Performance not found"));

        if (p.getState() != PerformanceState.CREATED) {
            throw new BusinessRuleException("Withdraw allowed only in CREATED state");
        }

        p.setState(PerformanceState.WITHDRAWN);
        return performanceRepository.save(p);
    }
}
