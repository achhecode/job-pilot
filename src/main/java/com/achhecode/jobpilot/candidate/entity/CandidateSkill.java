package com.achhecode.jobpilot.candidate.entity;

import com.achhecode.jobpilot.common.enums.ProficiencyLevel;
import com.achhecode.jobpilot.skill.entity.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "candidate_skills",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_candidate_skill",
        columnNames = {"candidate_id", "skill_id"}
    ),
    indexes = {
        @Index(
            name = "idx_candidate_skills_skill_candidate",
            columnList = "skill_id,candidate_id"
        ),
        @Index(
            name = "idx_candidate_skills_experience",
            columnList = "skill_id,experience_months"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateProfile candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "experience_months", nullable = false)
    private Integer experienceMonths = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", length = 30)
    private ProficiencyLevel proficiencyLevel;

    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;

    @Column(name = "is_primary_skill", nullable = false)
    private Boolean primarySkill = false;

    @Column(name = "years_of_experience_source", length = 30)
    private String experienceSource = "CANDIDATE_REPORTED";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
