package com.verbrix.payload.interpretercontroller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.verbrix.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationStatusResponse {

    private VerificationStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rejectionReason;

    private boolean available;
}
