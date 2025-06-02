package com.cas.login.repository;

import com.cas.login.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
    Set<Role> findAllByNameIn(Set<String> names);
}
