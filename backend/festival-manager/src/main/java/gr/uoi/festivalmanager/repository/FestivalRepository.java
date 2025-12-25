package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long> {}
