package main.repository;

import main.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    @Query(value = "SELECT * FROM roles r WHERE r.name = :role", nativeQuery = true)
    Optional<Role> findByName(@Param("role") String role);
}
