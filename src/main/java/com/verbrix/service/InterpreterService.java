package com.verbrix.service;

import com.verbrix.payload.interpretercontroller.request.InterpreterApplicationRequest;
import com.verbrix.payload.interpretercontroller.request.SetConsultationFeesRequest;
import com.verbrix.payload.interpretercontroller.response.ApplicationStatusResponse;
import com.verbrix.payload.interpretercontroller.response.InterpreterApplicationResponse;
import com.verbrix.payload.publiccontroller.response.InterpreterAuthenticatedResponse;
import com.verbrix.payload.publiccontroller.response.InterpreterPublicResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface InterpreterService {
    InterpreterApplicationResponse apply(String userEmail, InterpreterApplicationRequest request);
    InterpreterApplicationResponse reApply(String userEmail, InterpreterApplicationRequest request);
    ApplicationStatusResponse getApplicationStatus(String userEmail);

    @Transactional(readOnly = true)
    InterpreterApplicationResponse getMyApplication(String userEmail);

    Page<InterpreterPublicResponse> getVerifiedAvailableInterpreters(Pageable pageable);

    InterpreterAuthenticatedResponse getAuthenticatedInterpreterById(Long id);


    void updateConsultationFees(@Valid SetConsultationFeesRequest consultationFees, String username);
}
