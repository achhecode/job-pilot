package com.achhecode.jobpilot.application.repository;

import com.achhecode.jobpilot.application.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
}
