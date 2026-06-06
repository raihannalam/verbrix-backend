package com.verbrix.repository;

import com.verbrix.model.enums.RoleType;
import com.verbrix.model.rbac.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleType name);
}
