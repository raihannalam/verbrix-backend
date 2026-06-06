package com.verbrix.payload.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private String razorpayOrderId;
    private String currency;
    private Integer amount; // in paise
    private String keyId; // Public Key for frontend
}