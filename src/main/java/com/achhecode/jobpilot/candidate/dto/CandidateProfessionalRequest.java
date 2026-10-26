package com.achhecode.jobpilot.candidate.dto;

import com.achhecode.jobpilot.common.enums.EmploymentStatus;
import com.achhecode.jobpilot.common.enums.NoticePeriodType;
import com.achhecode.jobpilot.common.enums.WorkMode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CandidateProfessionalRequest(

    @NotNull
    @PositiveOrZero
    Integer totalExperienceMonths,

    @PositiveOrZero
    Integer noticePeriodDays,

    NoticePeriodType noticePeriodType,

    @DecimalMin("0.0")
    BigDecimal currentCtc,

    @DecimalMin("0.0")
    BigDecimal expectedCtc,

    @Size(min = 3, max = 3)
    String currencyCode,

    @NotNull
    EmploymentStatus employmentStatus,

    WorkMode preferredWorkMode,

    Boolean willingToRelocate
) {}
