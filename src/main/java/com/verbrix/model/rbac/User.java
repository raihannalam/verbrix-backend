package com.verbrix.model.rbac;

import com.verbrix.model.profile.Client;
import com.verbrix.model.profile.Interpreter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @Email
    private String email;

    @Column(unique = true)
    private String phone;

    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = false;

    private String providerId;

    @Column(length = 50)
    private String authProvider;

    @Column(name = "otp_code")
    private String otp;

    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    @Builder.Default
    @Column(name = "otp_attempts", nullable = false)
    private int otpAttempts = 0;

    @Column(name = "otp_locked_until")
    private Instant otpLockedUntil;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt; // Hibernate will set it

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt; // Hibernate will set it

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Client clientProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Interpreter interpreterProfile;

}