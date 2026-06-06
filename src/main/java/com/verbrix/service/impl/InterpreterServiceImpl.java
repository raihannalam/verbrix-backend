package com.verbrix.service.impl;

import com.verbrix.exception.ConflictException;
import com.verbrix.exception.ResourceNotFoundException;
import com.verbrix.payload.interpretercontroller.request.SetConsultationFeesRequest;
import com.verbrix.payload.publiccontroller.response.InterpreterAuthenticatedResponse;
import com.verbrix.payload.publiccontroller.response.InterpreterPublicResponse;
import com.verbrix.service.helpers.interpreterservice.InterpreterMappingHelper;
import com.verbrix.service.helpers.interpreterservice.InterpreterPublicMapper;
import com.verbrix.service.helpers.interpreterservice.InterpreterValidationHelper;
import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.profile.Interpreter;
import com.verbrix.model.rbac.User;
import com.verbrix.payload.interpretercontroller.request.InterpreterApplicationRequest;
import com.verbrix.payload.interpretercontroller.response.ApplicationStatusResponse;
import com.verbrix.payload.interpretercontroller.response.InterpreterApplicationResponse;
import com.verbrix.repository.InterpreterRepository;
import com.verbrix.repository.UserRepository;
import com.verbrix.service.InterpreterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InterpreterServiceImpl implements InterpreterService {

    private final UserRepository userRepository;
    private final InterpreterValidationHelper interpreterValidationHelper;
    private final InterpreterMappingHelper interpreterMappingHelper;

    private final InterpreterRepository interpreterRepository;
    private final InterpreterPublicMapper interpreterPublicMapper;

    @Override
    @Transactional
    public InterpreterApplicationResponse apply(String userEmail, InterpreterApplicationRequest request) {
        log.info("Processing interpreter application request for user {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Interpreter existingInterpreter = interpreterRepository.findByUser(user).orElse(null);

        interpreterValidationHelper.validateApply(existingInterpreter);


        Interpreter interpreter = new Interpreter();
        interpreter.setUser(user);

        interpreterMappingHelper.mapApplicationRequest(interpreter, request);

        interpreter.setStatus(VerificationStatus.PENDING);
        interpreter.setAvailable(false);
        interpreter.setRejectionReason(null);


        Interpreter saved = interpreterRepository.save(interpreter);

        interpreterMappingHelper.confirmSafely(request.getProfilePictureUrl());
        interpreterMappingHelper.confirmSafely(request.getGovernmentIdUrl());
        if (request.getCertifications() != null) {
            request.getCertifications()
                    .forEach(cert -> interpreterMappingHelper.confirmSafely(cert.getFileUrl()));
        }

        return interpreterMappingHelper.toApplicationResponse(saved);


    }

    @Override
    public InterpreterApplicationResponse reApply(String userEmail, InterpreterApplicationRequest request) {
        log.info("Processing interpreter re-apply request for user {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Interpreter interpreter = interpreterRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found"));

        if (interpreter.getStatus() != VerificationStatus.CHANGES_REQUESTED) {
            throw new ConflictException("Re-apply not supported");
        }
        interpreterMappingHelper.mapApplicationRequest(interpreter, request);
        interpreter.setStatus(VerificationStatus.PENDING);
        interpreter.setAvailable(false);
        interpreter.setRejectionReason(null);

        Interpreter saved = interpreterRepository.save(interpreter);

        return interpreterMappingHelper.toApplicationResponse(saved);

    }

    @Override
    public ApplicationStatusResponse getApplicationStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Interpreter interpreter = interpreterRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter not found"));

        return ApplicationStatusResponse.builder()
                .status(interpreter.getStatus())
                .available(interpreter.isAvailable())
                .rejectionReason(interpreter.getRejectionReason())
                .build();

    }

    @Transactional(readOnly = true)
    @Override
    public InterpreterApplicationResponse getMyApplication(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Interpreter interpreter = interpreterRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Reuse your existing mapper
        return interpreterMappingHelper.toApplicationResponse(interpreter);
    }

    @Override
    public Page<InterpreterPublicResponse> getVerifiedAvailableInterpreters(Pageable pageable) {

        Page<Interpreter> page = interpreterRepository.findAllByStatusAndAvailableTrue(VerificationStatus.VERIFIED, pageable);
        return page.map(interpreterPublicMapper::toPublic);
    }

    @Override
    @Transactional(readOnly = true)
    public InterpreterAuthenticatedResponse getAuthenticatedInterpreterById(Long id) {
        Interpreter interpreter = interpreterRepository.findByIdWithDetails(id).orElseThrow(() -> new ResourceNotFoundException("Interpreter not found"));
        return interpreterPublicMapper.toAuthenticated(interpreter);
    }

    @Override
    public void updateConsultationFees(SetConsultationFeesRequest request, String username) {

        log.info("Updating consultation fees for user {}", username);
        User user = userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Interpreter interpreter = interpreterRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Interpreter not found"));

        if (interpreter.getStatus() != VerificationStatus.VERIFIED) {
            throw new ConflictException("Consultation fees can only be updated after verification");
        }
        if (request.getConsultationFees() ==  null) {
            throw new ConflictException("Consultation fees cannot be null");
        }

        BigDecimal fee = request.getConsultationFees();

        if (fee.scale() == 0) {
            fee = fee.setScale(2, RoundingMode.UNNECESSARY);
        }

        interpreter.setConsultationFee(fee);
        interpreterRepository.save(interpreter);
        log.info("Consultation fees updated successfully");
    }
}