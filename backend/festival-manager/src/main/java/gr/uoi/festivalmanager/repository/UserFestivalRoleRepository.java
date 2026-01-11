package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.entity.UserFestivalRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFestivalRoleRepository extends JpaRepository<UserFestivalRole, UserFestivalRoleId> {

    boolean existsByIdUserIdAndIdFestivalId(Long userId, Long festivalId);
    
    boolean existsByIdUserIdAndIdFestivalIdAndRole_Name(Long userId, Long festivalId, String roleName);

    @Query("select ufr.role.name from UserFestivalRole ufr where ufr.id.userId = :userId and ufr.id.festivalId = :festivalId")
    Optional<String> findRoleNameForUserInFestival(Long userId, Long festivalId);

    @Query("select ufr from UserFestivalRole ufr join fetch ufr.role join fetch ufr.festival where ufr.user.id = :userId")
    List<UserFestivalRole> findAllByUserId(Long userId);
}
