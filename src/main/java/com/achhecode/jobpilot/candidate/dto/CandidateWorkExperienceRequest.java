package com.achhecode.jobpilot.candidate.dto;

import com.achhecode.jobpilot.common.enums.EmploymentType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CandidateWorkExperienceRequest(

    @NotBlank
    @Size(max = 200)
    String companyName,

    @NotBlank
    @Size(max = 200)
    String jobTitle,

    EmploymentType employmentType,

    @NotNull
    LocalDate startDate,

    LocalDate endDate,

    Boolean current,

    String description,

    @Size(max = 200)
    String location
) {}
