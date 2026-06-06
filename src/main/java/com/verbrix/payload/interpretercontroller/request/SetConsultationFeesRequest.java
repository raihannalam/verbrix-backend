package com.verbrix.payload.interpretercontroller.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SetConsultationFeesRequest {
    private BigDecimal consultationFees;
}
