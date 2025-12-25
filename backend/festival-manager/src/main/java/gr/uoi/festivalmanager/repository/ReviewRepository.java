package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {}
