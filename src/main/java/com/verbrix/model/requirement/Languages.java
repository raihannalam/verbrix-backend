package com.verbrix.model.requirement;

import com.verbrix.model.enums.Language;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "languages")
public class Languages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    private Language name;
}