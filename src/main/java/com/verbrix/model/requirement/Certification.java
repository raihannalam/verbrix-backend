package com.verbrix.model.requirement;

import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.profile.Interpreter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "interpreter")
@Table(name = "certifications")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "issuing_organization", nullable = false)
    private String issuingOrganization; // e.g., "CCHI", "National Board", "ATA"

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Lob
    private String description;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // CRITICAL: Used to trigger re-verification alerts

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status; // PENDING, VERIFIED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interpreter_id", nullable = false)
    private Interpreter interpreter;


    @Column(name = "rejection_reason")
    private String rejectionReason;

}