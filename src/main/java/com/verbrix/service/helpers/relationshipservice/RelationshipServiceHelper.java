package com.verbrix.service.helpers.relationshipservice;

import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import com.verbrix.payload.relationship.RelationshipResponse;
import com.verbrix.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class RelationshipServiceHelper {

    private final RelationshipRepository relationshipRepository;

    public RelationshipResponse toResponse(Relationship r) {
        String clientName = "Unknown Client";
        if (r.getClient() != null) {
            if (r.getClient().getClientProfile() != null) {
                clientName =
                        r.getClient().getClientProfile().getFirstName() + " " + r.getClient().getClientProfile().getLastName();
            } else {
                clientName = r.getClient().getEmail();
            }
        }

        String interpreterName = "Unknown Interpreter";
        if (r.getInterpreter() != null) {
            if (r.getInterpreter().getInterpreterProfile() != null) {
                interpreterName =
                        r.getInterpreter().getInterpreterProfile().getFirstName() + " " + r.getInterpreter().getInterpreterProfile().getLastName();
            } else {
                interpreterName = r.getInterpreter().getEmail();
            }
        }
        return RelationshipResponse.builder()
                .relationshipId(r.getId())
                .clientId(r.getClient().getId())
                .clientName(clientName)
                .interpreterId(r.getInterpreter().getId())
                .interpreterName(interpreterName)
                .status(r.getStatus())
                .initialMessage(r.getInitialMessage())
                .createdAt(r.getCreatedAt())
                .build();
    }


    public Relationship getRelationship(Long relationshipId) {
        return relationshipRepository.findById(relationshipId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Relationship not found: " + relationshipId
                        )
                );
    }


    public BigDecimal resolveConsultationFeeHelper(Relationship relationship) {

        User interpreterUser = relationship.getInterpreter();
        if (interpreterUser == null) {
            throw new IllegalStateException("Interpreter missing in relationship");
        }

        Interpreter interpreterProfile = interpreterUser.getInterpreterProfile();
        if (interpreterProfile == null) {
            throw new IllegalStateException("Interpreter profile missing in user");
        }

        BigDecimal consultationFee = interpreterProfile.getConsultationFee();
        if (consultationFee == null || consultationFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Consultation fee not configured for interpreter");
        }

        return consultationFee;
    }


    public BigDecimal resolveServiceAgreementFeeHelper(Relationship relationship) {

        User interpreterUser = relationship.getInterpreter();
        if (interpreterUser == null) {
            throw new IllegalStateException("Interpreter missing in relationship");
        }

        Interpreter interpreterProfile = interpreterUser.getInterpreterProfile();
        if (interpreterProfile == null) {
            throw new IllegalStateException("Interpreter profile missing for user");
        }

        BigDecimal agreementFee = interpreterProfile.getServiceAgreementFee();
        if (agreementFee == null || agreementFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Service agreement fee not configured for interpreter"
            );
        }

        return agreementFee;
    }


}
