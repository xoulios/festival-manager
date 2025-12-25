package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Performance;

public interface PerformanceService {

    Performance createPerformance(Long festivalId, Long artistId, Performance performance);

    Performance updatePerformance(Long performanceId, Performance updated);

    Performance submitPerformance(Long performanceId);

    Performance withdrawPerformance(Long performanceId);
}
