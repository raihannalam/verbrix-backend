package com.verbrix.service;

import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.payload.admininterpretercontroller.request.AdminRemarkRequest;
import com.verbrix.payload.admininterpretercontroller.response.ActionSuccessResponse;
import com.verbrix.payload.admininterpretercontroller.response.CertificationStatusResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterDetailResponse;
import com.verbrix.payload.admininterpretercontroller.response.InterpreterSummaryResponse;

import java.util.List;

public interface AdminInterpreterService {
    // Returns list for the main table
    List<InterpreterSummaryResponse> getAllInterpreters(VerificationStatus status);

    InterpreterDetailResponse getInterpreterDetails(Long id);

    ActionSuccessResponse approveInterpreter(Long id);

    ActionSuccessResponse requestChanges(Long id, AdminRemarkRequest request);

    ActionSuccessResponse rejectPermanently(Long id, AdminRemarkRequest request);

    // Certification specific responses
    CertificationStatusResponse verifyCertification(Long certificationId);

    CertificationStatusResponse rejectCertification(Long certificationId, AdminRemarkRequest request);
}