package com.achhecode.jobpilot.candidate.repository;

import com.achhecode.jobpilot.candidate.entity.CandidateProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateProfessionalProfileRepository
        extends JpaRepository<CandidateProfessionalProfile, Long> {
}
