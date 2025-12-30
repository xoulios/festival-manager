package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findTopByPerformanceIdOrderByIdDesc(Long performanceId);
}
