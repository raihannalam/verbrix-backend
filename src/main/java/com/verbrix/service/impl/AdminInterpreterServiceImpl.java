package com.verbrix.service.impl;

import com.verbrix.exception.ConflictException;
import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.model.enums.RoleType;
import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.Role;
import com.verbrix.model.rbac.User;
import com.verbrix.model.requirement.Certification;
import com.verbrix.payload.admininterpretercontroller.request.AdminRemarkRequest;
import com.verbrix.payload.admininterpretercontroller.response.ActionSuccessResponse;
import com.verbrix.payload.admininterpretercontroller.response.CertificationStatusResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterDetailResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterSummaryResponse;
import com.verbrix.repository.CertificationRepository;
import com.verbrix.repository.InterpreterRepository;
import com.verbrix.repository.RoleRepository;
import com.verbrix.repository.UserRepository;
import com.verbrix.service.AdminInterpreterService;
import com.verbrix.service.EmailService;
import com.verbrix.service.helpers.interpreteradminservice.AdminMappingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInterpreterServiceImpl implements AdminInterpreterService {

    private final InterpreterRepository interpreterRepository;
    private final AdminMappingHelper adminMappingHelper;
    private final CertificationRepository certificationRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public List<InterpreterSummaryResponse> getAllInterpreters(VerificationStatus status) {
        return (status != null
                ? interpreterRepository.findAllByStatus(status)
                : interpreterRepository.findAll())
                .stream()
                .map(adminMappingHelper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InterpreterDetailResponse getInterpreterDetails(Long id) {
        Interpreter interpreter = interpreterRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found with id: " + id));
        return adminMappingHelper.toDetailResponse(interpreter);
    }

    @Override
    @Transactional
    public ActionSuccessResponse approveInterpreter(Long id) {
        Interpreter interpreter = interpreterRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found with id: " + id));

        if (interpreter.getStatus() == VerificationStatus.VERIFIED) {
            throw new ConflictException("Interpreter is already verified");
        }

        boolean allVerified = !interpreter.getCertifications().isEmpty()
                && interpreter.getCertifications()
                .stream()
                .allMatch(c -> c.getStatus() == VerificationStatus.VERIFIED);

        if (!allVerified) {
            throw new ConflictException("All certifications must be verified first");
        }

        interpreter.setStatus(VerificationStatus.VERIFIED);
        interpreter.setAvailable(true);
        interpreter.setRejectionReason(null);

        User user = requireUser(interpreter);

        Role interpreterRole = roleRepository.findByName(RoleType.INTERPRETER)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter role not found"));

        user.getRoles().clear();
        user.getRoles().add(interpreterRole);

        userRepository.save(user);
        interpreterRepository.save(interpreter);

        emailService.sendApprovalEmail(
                user.getEmail(),
                interpreter.getFirstName()
        );

        return ActionSuccessResponse.builder()
                .interpreterId(id)
                .newStatus(VerificationStatus.VERIFIED)
                .message("Successfully approved")
                .timestamp(Instant.now())
                .build();
    }

    @Override
    @Transactional
    public ActionSuccessResponse requestChanges(Long id, AdminRemarkRequest request) {
        Interpreter interpreter = interpreterRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found with id: " + id));

        if (interpreter.getStatus() != VerificationStatus.PENDING) {
            throw new ConflictException("Only PENDING applications can request changes");
        }

        interpreter.setStatus(VerificationStatus.CHANGES_REQUESTED);
        interpreter.setAvailable(false);
        interpreter.setRejectionReason(request.getMessage());

        interpreterRepository.save(interpreter);

        User user = requireUser(interpreter);

        emailService.sendChangesRequestedEmail(
                user.getEmail(),
                interpreter.getFirstName(),
                request.getMessage()
        );

        return ActionSuccessResponse.builder()
                .interpreterId(id)
                .newStatus(VerificationStatus.CHANGES_REQUESTED)
                .message("Change request sent")
                .timestamp(Instant.now())
                .build();
    }

    @Override
    @Transactional
    public ActionSuccessResponse rejectPermanently(Long id, AdminRemarkRequest request) {
        Interpreter interpreter = interpreterRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found with id: " + id));

        if (interpreter.getStatus() == VerificationStatus.REJECTED) {
            throw new ConflictException("Interpreter already rejected");
        }
        if (interpreter.getStatus() == VerificationStatus.VERIFIED) {
            throw new ConflictException("Cannot reject a verified interpreter");
        }

        interpreter.setStatus(VerificationStatus.REJECTED);
        interpreter.setAvailable(false);
        interpreter.setRejectionReason(request.getMessage());

        interpreterRepository.save(interpreter);

        User user = requireUser(interpreter);

        emailService.sendRejectionEmail(
                user.getEmail(),
                interpreter.getFirstName(),
                request.getMessage()
        );

        return ActionSuccessResponse.builder()
                .interpreterId(id)
                .newStatus(VerificationStatus.REJECTED)
                .message("Interpreter permanently rejected")
                .timestamp(Instant.now())
                .build();
    }

    @Override
    @Transactional
    public CertificationStatusResponse verifyCertification(Long certificationId) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));

        if (certification.getStatus() == VerificationStatus.VERIFIED) {
            throw new ConflictException("Certification already verified");
        }

        certification.setStatus(VerificationStatus.VERIFIED);
        certification.setRejectionReason(null);

        certificationRepository.save(certification);

        return CertificationStatusResponse.builder()
                .certificationId(certificationId)
                .status(VerificationStatus.VERIFIED)
                .adminNote("Certification verified")
                .build();
    }

    @Override
    @Transactional
    public CertificationStatusResponse rejectCertification(Long certificationId, AdminRemarkRequest request) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));

        if (certification.getStatus() == VerificationStatus.VERIFIED) {
            throw new ConflictException("Certification already verified");
        }

        certification.setStatus(VerificationStatus.REJECTED);
        certification.setRejectionReason(request.getMessage());

        certificationRepository.save(certification);

        Interpreter interpreter = certification.getInterpreter();
        User user = requireUser(interpreter);

        if (interpreter.getStatus() == VerificationStatus.PENDING) {
            interpreter.setStatus(VerificationStatus.CHANGES_REQUESTED);
            interpreter.setAvailable(false);
            interpreter.setRejectionReason("Certification rejected: " + request.getMessage());
            interpreterRepository.save(interpreter);
        }

        emailService.sendChangesRequestedEmail(
                user.getEmail(),
                interpreter.getFirstName(),
                "Certification rejected: " + request.getMessage()
        );

        return CertificationStatusResponse.builder()
                .certificationId(certificationId)
                .status(VerificationStatus.REJECTED)
                .adminNote("Certification rejected and interpreter notified")
                .build();
    }

    private User requireUser(Interpreter interpreter) {
        User user = interpreter.getUser();
        if (user == null) {
            throw new IllegalStateException("Interpreter has no linked user account");
        }
        return user;
    }
}
