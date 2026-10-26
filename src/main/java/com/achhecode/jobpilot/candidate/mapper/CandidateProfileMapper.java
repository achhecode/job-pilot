package com.achhecode.jobpilot.candidate.mapper;

import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.achhecode.jobpilot.candidate.entity.CandidateProfile;

public final class CandidateProfileMapper {

    private CandidateProfileMapper() {
    }

    public static CandidateProfileResponse toResponse(
            CandidateProfile candidate
    ) {
        if (candidate == null) {
            return null;
        }

        return CandidateProfileResponse.builder()
                .id(candidate.getId())
                .username(candidate.getUsername())
                .fullName(candidate.getFullName())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhoneNumber())
                .gender(candidate.getGender())
                .dateOfBirth(candidate.getDateOfBirth())
                .countryCode(candidate.getCountryCode())
                .stateRegion(candidate.getStateRegion())
                .city(candidate.getCity())
                .postalCode(candidate.getPostalCode())
                .addressLine1(candidate.getAddressLine1())
                .addressLine2(candidate.getAddressLine2())
                .professionalSummary(candidate.getProfessionalSummary())
                .linkedinUrl(candidate.getLinkedinUrl())
                .githubUrl(candidate.getGithubUrl())
                .portfolioUrl(candidate.getPortfolioUrl())
                .profileStatus(candidate.getProfileStatus())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                // Map these when their source fields are available:
                .resumePath(null)
                .keywords(null)
                .build();
    }
}
