package com.achhecode.jobpilot.candidate.repository;

import com.achhecode.jobpilot.candidate.entity.CandidateWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateWorkExperienceRepository
        extends JpaRepository<CandidateWorkExperience, Long> {

    List<CandidateWorkExperience>
        findByCandidate_IdOrderByStartDateDesc(Long candidateId);

    List<CandidateWorkExperience>
        findByCandidate_IdAndCurrentTrue(Long candidateId);
}
