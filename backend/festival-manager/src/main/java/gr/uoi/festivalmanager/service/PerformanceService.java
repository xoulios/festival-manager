package gr.uoi.festivalmanager.service;

import gr.uoi.festivalmanager.entity.Performance;
import gr.uoi.festivalmanager.dto.FinalSubmitRequest;
import gr.uoi.festivalmanager.dto.ReviewRequest;
import java.util.List;


public interface PerformanceService {

    Performance createPerformance(Long festivalId, Long artistId, Performance performance);

    Performance updatePerformance(Long performanceId, Performance updated);

    Performance submitPerformance(Long performanceId);

    Performance withdrawPerformance(Long performanceId);

    Performance reviewPerformance(Long performanceId, Long staffId, ReviewRequest request);
    
    Performance approvePerformance(Long performanceId, Long staffId);

    Performance rejectPerformance(Long performanceId, Long staffId, String reason);

    Performance schedulePerformance(Long performanceId, Long schedulerId, String scheduledSlot);

    Performance finalSubmitPerformance(Long performanceId, Long artistId, FinalSubmitRequest request);
    
    List<Performance> searchPerformances(Long festivalId, String query);
}
