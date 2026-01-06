package gr.uoi.festivalmanager.repository;

import gr.uoi.festivalmanager.entity.UserFestivalRole;
import gr.uoi.festivalmanager.entity.UserFestivalRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFestivalRoleRepository extends JpaRepository<UserFestivalRole, UserFestivalRoleId> {

    boolean existsByIdUserIdAndIdFestivalIdAndRole_Name(Long userId, Long festivalId, String roleName);
    boolean existsByIdUserIdAndIdFestivalId(Long userId, Long festivalId);
    boolean existsByUser_IdAndFestival_IdAndRole_Name(Long userId, Long festivalId, String roleName);

    @Query("""
        select (count(ufr) > 0) 
        from UserFestivalRole ufr
        where ufr.user.id = :userId
          and ufr.festival.id = :festivalId
          and lower(ufr.role.name) = lower(:roleName)
    """)
    boolean existsByUserIdAndFestivalIdAndRoleName(Long userId, Long festivalId, String roleName);

    @Query("""
        select ufr.festival.id, ufr.role.name
        from UserFestivalRole ufr
        where ufr.user.id = :userId
    """)
    List<Object[]> findFestivalRoles(Long userId);

    @Query("""
        select ufr.role.name
        from UserFestivalRole ufr
        where ufr.user.id = :userId and ufr.festival.id = :festivalId
    """)
    Optional<String> findRoleNameForUserInFestival(Long userId, Long festivalId);
}
