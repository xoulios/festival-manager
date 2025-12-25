package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {}
