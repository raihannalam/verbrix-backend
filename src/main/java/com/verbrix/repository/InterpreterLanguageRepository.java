package com.verbrix.repository;

import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.requirement.InterpreterLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterpreterLanguageRepository extends JpaRepository<InterpreterLanguage, Long> {

    List<InterpreterLanguage> findByInterpreter(Interpreter interpreter);
}
