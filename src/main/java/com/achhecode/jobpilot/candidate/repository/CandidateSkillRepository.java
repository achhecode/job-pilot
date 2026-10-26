package com.achhecode.jobpilot.candidate.repository;

import com.achhecode.jobpilot.candidate.entity.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateSkillRepository
        extends JpaRepository<CandidateSkill, Long> {

    List<CandidateSkill> findByCandidate_Id(Long candidateId);

    boolean existsByCandidate_IdAndSkill_Id(
        Long candidateId,
        Long skillId
    );
}
