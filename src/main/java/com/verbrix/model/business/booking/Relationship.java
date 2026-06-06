package com.verbrix.model.business.booking;

import com.verbrix.model.enums.RelationshipStatus;
import com.verbrix.model.rbac.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "relationships",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_id", "interpreter_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --------------------
    // PARTICIPANTS
    // --------------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interpreter_id", nullable = false)
    private User interpreter;

    // --------------------
    // LIFECYCLE (SINGLE SOURCE OF TRUTH)
    // --------------------

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipStatus status;

    // --------------------
    // CONSULTATION METADATA
    // --------------------

    /** Set when consultation payment succeeds */
    private Instant consultationStartedAt;

    // --------------------
    // MESSAGING
    // --------------------

    @Column(length = 1000)
    private String initialMessage;

    // --------------------
    // AUDIT
    // --------------------

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // --------------------
    // DOMAIN HELPERS
    // --------------------

    public boolean isClient(String email) {
        return client != null && client.getEmail().equals(email);
    }

    public boolean isInterpreter(String email) {
        return interpreter != null && interpreter.getEmail().equals(email);
    }

    public boolean isParticipant(String email) {
        return isClient(email) || isInterpreter(email);
    }


    public boolean isCallingAllowed() {
        return switch (this.status) {
            case CONSULTATION_ACTIVE,
                 CONSULTATION_COMPLETED,
                 AGREEMENT_ACTIVE,
                 WORK_ACTIVE -> true;
            default -> false;
        };
    }

}
