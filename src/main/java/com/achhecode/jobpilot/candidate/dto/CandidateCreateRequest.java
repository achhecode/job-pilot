package com.achhecode.jobpilot.candidate.dto;

import com.achhecode.jobpilot.common.enums.Gender;
import com.achhecode.jobpilot.common.enums.ProfileStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record CandidateCreateRequest(

    @NotBlank
    @Size(max = 100)
    String username,

    @NotBlank
    @Size(max = 200)
    String fullName,

    @Email
    @Size(max = 254)
    String email,

    @Size(max = 30)
    String phoneNumber,

    Gender gender,

    @Past
    LocalDate dateOfBirth,

    @Size(min = 2, max = 2)
    String countryCode,

    @Size(max = 120)
    String stateRegion,

    @Size(max = 120)
    String city,

    @Size(max = 20)
    String postalCode,

    @Size(max = 255)
    String addressLine1,

    @Size(max = 255)
    String addressLine2,

    String professionalSummary,

    @Size(max = 500)
    String linkedinUrl,

    @Size(max = 500)
    String githubUrl,

    @Size(max = 500)
    String portfolioUrl,

    ProfileStatus profileStatus,

    @Valid
    CandidateProfessionalRequest professionalProfile,

    @Valid
    List<CandidateWorkExperienceRequest> workExperiences,

    @Valid
    List<CandidateSkillRequest> skills
) {}
