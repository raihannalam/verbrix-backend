package com.verbrix.service.helpers.interpreterservice;

import com.verbrix.exception.ConflictException;
import com.verbrix.model.enums.VerificationStatus;
import com.verbrix.model.profile.Interpreter;
import org.springframework.stereotype.Component;

@Component
public class InterpreterValidationHelper {

    // Change this method in InterpreterValidationHelper.java
    public void validateApply(Interpreter existingInterpreter) {
        if (existingInterpreter != null) {
            if (existingInterpreter.getStatus() == VerificationStatus.REJECTED) {
                throw new ConflictException(
                        "This account has been permanently rejected. You cannot apply again."
                );
            }
            throw new ConflictException(
                    "Interpreter application already exists. Check application status"
            );
        }
    }

    public void validateReApply(Interpreter interpreter) {
        if (interpreter.getStatus() == VerificationStatus.REJECTED) {
            throw new ConflictException(
                    "Application is permanently rejected"
            );
        }

        if (interpreter.getStatus() != VerificationStatus.CHANGES_REQUESTED) {
            throw new ConflictException(
                    "Re-apply is allowed only when changes are requested by admin"
            );
        }
    }
}

