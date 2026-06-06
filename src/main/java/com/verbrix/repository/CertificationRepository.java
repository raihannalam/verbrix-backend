package com.verbrix.repository;

import com.verbrix.model.requirement.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {;
}
