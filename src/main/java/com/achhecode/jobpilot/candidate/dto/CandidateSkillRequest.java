package com.achhecode.jobpilot.candidate.dto;

import com.achhecode.jobpilot.common.enums.ProficiencyLevel;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CandidateSkillRequest(

    @NotNull
    @Positive
    Long skillId,

    @NotNull
    @PositiveOrZero
    Integer experienceMonths,

    ProficiencyLevel proficiencyLevel,

    LocalDate lastUsedDate,

    Boolean primarySkill
) {}
