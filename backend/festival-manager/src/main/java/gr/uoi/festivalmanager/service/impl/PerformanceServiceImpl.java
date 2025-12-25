package gr.uoi.festivalmanager.service.impl;

import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.enums.PerformanceState;
import gr.uoi.festivalmanager.repository.PerformanceRepository;
import gr.uoi.festivalmanager.service.PerformanceService;
import org.springframework.stereotype.Service;

@Service
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;

    public PerformanceServiceImpl(PerformanceRepository performanceRepository) {
        this.performanceRepository = performanceRepository;
    }

    @Override
    public Performance createPerformance(Long festivalId, Long artistId, Performance performance) {
        performance.setState(PerformanceState.CREATED);
        return performanceRepository.save(performance);
    }

    @Override
    public Performance updatePerformance(Long performanceId, Performance updated) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new RuntimeException("Performance not found"));

        p.setName(updated.getName());
        p.setDescription(updated.getDescription());

        return performanceRepository.save(p);
    }

    @Override
    public Performance submitPerformance(Long performanceId) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new RuntimeException("Performance not found"));

        if (p.getState() != PerformanceState.CREATED) {
            throw new RuntimeException("Only CREATED performances can be submitted");
        }

        p.setState(PerformanceState.SUBMITTED);
        return performanceRepository.save(p);
    }

    @Override
    public Performance withdrawPerformance(Long performanceId) {
        Performance p = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new RuntimeException("Performance not found"));

        if (p.getState() != PerformanceState.SUBMITTED) {
            throw new RuntimeException("Only SUBMITTED performances can be withdrawn");
        }

        p.setState(PerformanceState.WITHDRAWN);
        return performanceRepository.save(p);
    }
}
