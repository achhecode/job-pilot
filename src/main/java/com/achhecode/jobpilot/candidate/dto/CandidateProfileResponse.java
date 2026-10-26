
package com.achhecode.jobpilot.candidate.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.achhecode.jobpilot.common.enums.Gender;
import com.achhecode.jobpilot.common.enums.ProfileStatus;

import lombok.Builder;

@Builder
public record CandidateProfileResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        Gender gender,
        LocalDate dateOfBirth,
        String countryCode,
        String stateRegion,
        String city,
        String postalCode,
        String addressLine1,
        String addressLine2,
        String professionalSummary,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        String resumePath,
        String keywords,
        ProfileStatus profileStatus,
        Instant createdAt,
        Instant updatedAt) {
}
