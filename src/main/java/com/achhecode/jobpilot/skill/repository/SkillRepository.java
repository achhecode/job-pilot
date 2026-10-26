package com.achhecode.jobpilot.skill.repository;

import com.achhecode.jobpilot.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);

    List<Skill> findByCategoryAndActiveTrue(String category);

    List<Skill> findByActiveTrue();
}
