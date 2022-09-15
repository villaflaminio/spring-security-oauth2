package com.flaminiovilla.security.repository;

import com.flaminiovilla.security.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository to manage all the operations related to the role.
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);

}