package com.SWD_G4.OrderFlow.repository;

import com.SWD_G4.OrderFlow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
    
    /**
     * Find all users with a specific role
     * @param roleName the role name (e.g., "FLORIST")
     * @return List of users with the specified role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.roles r WHERE r.name = :roleName AND u.email IS NOT NULL AND u.email != ''")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
