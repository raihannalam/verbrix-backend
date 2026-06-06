package com.verbrix.model.rbac;

import com.verbrix.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, unique = true, nullable = false)
    private RoleType name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> user;

}