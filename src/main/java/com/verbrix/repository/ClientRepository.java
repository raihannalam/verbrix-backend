package com.verbrix.repository;

import com.verbrix.model.profile.Client;
import com.verbrix.model.rbac.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByUser(User user);
}
