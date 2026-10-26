package com.achhecode.jobpilot.candidate.repository;

import com.achhecode.jobpilot.candidate.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProfileRepository
        extends JpaRepository<CandidateProfile, Long> {

    Optional<CandidateProfile> findByUsername(String username);

    boolean existsByUsername(String username);
}
