package com.verbrix.model.requirement;

import com.verbrix.model.enums.ProficiencyLevel;
import com.verbrix.model.profile.Interpreter;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class InterpreterLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiencyLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interpreter_id")
    private Interpreter interpreter;

    @ManyToOne
    @JoinColumn(name = "langauge_id")
    private Languages language;
}
