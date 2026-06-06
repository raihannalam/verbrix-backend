package com.verbrix.model.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.verbrix.model.rbac.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_devices")
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String deviceIdentifier;

    private String deviceDetails;
    private String location;
    private String ipAddress;
    private Instant lastLogin;

    @Builder.Default
    private boolean isVerified = false; // For "Trusted Device" logic later
}