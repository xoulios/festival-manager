package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.entity.UserFestivalRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFestivalRoleRepository extends JpaRepository<UserFestivalRole, UserFestivalRoleId> {}
