package com.verbrix.payload.interpretercontroller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequest {

    @NotBlank(message = "Certification name is required")
    private String name;

    @NotBlank(message = "Issuing organization is required")
    private String issuingOrganization;

    @NotBlank(message = "The file link is missing")
    private String fileUrl;

    private String description;

    @NotNull(message = "Issue date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}