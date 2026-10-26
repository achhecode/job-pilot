package com.achhecode.jobpilot.application.service;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.application.dto.JobApplicationResponse;
import com.achhecode.jobpilot.application.model.ApplicationStatus;
import com.achhecode.jobpilot.application.model.JobApplication;
import com.achhecode.jobpilot.application.repository.JobApplicationRepository;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {
    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JobApplicationResponse create(JobApplicationRequest request) {
        JobApplication entity = new JobApplication(
                request.companyId().trim().toUpperCase(),
                request.jobUrl().trim(),
                request.jobTitle()
        );
        return toResponse(repository.save(entity));
    }

    @Transactional
    public JobApplicationResponse updateResult(Long id, AutomationResult result) {
        JobApplication entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        entity.setStatus(ApplicationStatus.valueOf(result.status().name()));
        entity.setMessage(result.message());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse get(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    private JobApplicationResponse toResponse(JobApplication entity) {
        return new JobApplicationResponse(
                entity.getId(), entity.getCompanyId(), entity.getJobUrl(),
                entity.getJobTitle(), entity.getStatus(), entity.getMessage(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
