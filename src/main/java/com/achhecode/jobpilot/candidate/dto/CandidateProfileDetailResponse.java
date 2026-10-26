
package com.achhecode.jobpilot.candidate.dto;

import java.util.List;

public record CandidateProfileDetailResponse(
        CandidateProfileResponse profile,
        CandidateProfessionalResponse professionalProfile,
        List<CandidateWorkExperienceResponse> workExperiences,
        List<CandidateSkillResponse> skills,
        List<CandidateResumeResponse> resumes
) {
}
