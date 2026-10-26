package com.achhecode.jobpilot.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.application.dto.JobApplicationResponse;
import com.achhecode.jobpilot.application.model.ApplicationStatus;
import com.achhecode.jobpilot.application.model.JobApplication;
import com.achhecode.jobpilot.application.repository.JobApplicationRepository;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.service.JobApplicationAutomationService;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.achhecode.jobpilot.candidate.entity.CandidateProfile;
import com.achhecode.jobpilot.candidate.mapper.CandidateProfileMapper;
import com.achhecode.jobpilot.candidate.repository.CandidateProfileRepository;
import com.achhecode.playwright.browser.BrowserPageResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobApplicationAutomationService jobApplicationAutomationService;

    private final BrowserPageResolver browserPageResolver;

    @Transactional
    public JobApplicationResponse create(JobApplicationRequest request) {
        String jobId = request.jobId() == null
                || request.jobId().isBlank()
                        ? null
                        : request.jobId().trim().toUpperCase();

        String jobUrl = request.jobUrl() == null
                || request.jobUrl().isBlank()
                        ? null
                        : request.jobUrl().trim();

        CandidateProfile candidateProfile = candidateProfileRepository
                .findByUsername(request.candidateUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Candidate not found: " + request.candidateUsername()));

        JobApplication newJobApplication = new JobApplication(
                candidateProfile.getUsername(),
                jobId,
                jobUrl,
                "",
                ApplicationStatus.IN_PROGRESS,
                "");

        JobAutomationContext jobAutomationContext = new JobAutomationContext(
                browserPageResolver.findOrOpenPage(request.company().getUrl()));

        CandidateProfileResponse candidateProfileResponse = CandidateProfileMapper.toResponse(candidateProfile);

        if (request.submitAutomatically()) {
            jobApplicationAutomationService.apply(jobAutomationContext, request, candidateProfileResponse);
            newJobApplication.setStatus(ApplicationStatus.SUBMITTED);
        } else {
            newJobApplication.setStatus(ApplicationStatus.FAILED);
        }

        JobApplication savedApplication = jobApplicationRepository.save(newJobApplication);

        return toResponse(savedApplication);
    }

    @Transactional
    public JobApplicationResponse updateResult(Long id, AutomationResult result) {
        JobApplication entity = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));

        entity.setStatus(ApplicationStatus.valueOf(result.status().name()));
        entity.setMessage(result.message());

        return toResponse(jobApplicationRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse get(Long id) {
        return jobApplicationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    private JobApplicationResponse toResponse(JobApplication entity) {
        return new JobApplicationResponse(
                entity.getId(),
                entity.getJobId(),
                entity.getJobUrl(),
                entity.getJobTitle(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
