package com.verbrix.repository;

import com.verbrix.model.requirement.Languages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepository extends JpaRepository<Languages, Long> {

}
