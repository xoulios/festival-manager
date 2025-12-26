package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findByFestivalId(Long festivalId);
}
