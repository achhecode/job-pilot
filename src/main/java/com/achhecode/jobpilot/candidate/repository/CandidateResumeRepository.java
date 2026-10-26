package com.achhecode.jobpilot.candidate.repository;

import com.achhecode.jobpilot.candidate.entity.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateResumeRepository
        extends JpaRepository<CandidateResume, Long> {

    List<CandidateResume> findByCandidate_Id(Long candidateId);

    List<CandidateResume>
        findByCandidate_IdAndPrimaryTrue(Long candidateId);
}
