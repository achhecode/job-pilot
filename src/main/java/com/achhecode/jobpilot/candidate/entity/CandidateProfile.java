package com.achhecode.jobpilot.candidate.entity;

import com.achhecode.jobpilot.common.enums.Gender;
import com.achhecode.jobpilot.common.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "candidate_profiles",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_candidate_username",
        columnNames = "username"
    ),
    indexes = {
        @Index(name = "idx_candidate_full_name", columnList = "full_name"),
        @Index(
            name = "idx_candidate_location",
            columnList = "country_code,state_region,city"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(length = 254)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Gender gender;

    private LocalDate dateOfBirth;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "state_region", length = 120)
    private String stateRegion;

    @Column(length = 120)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "professional_summary", columnDefinition = "TEXT")
    private String professionalSummary;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false, length = 30)
    private ProfileStatus profileStatus = ProfileStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne(
        mappedBy = "candidate",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private CandidateProfessionalProfile professionalProfile;

    @OneToMany(
        mappedBy = "candidate",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CandidateWorkExperience> workExperiences =
        new ArrayList<>();

    @OneToMany(
        mappedBy = "candidate",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CandidateSkill> skills = new ArrayList<>();

    @OneToMany(
        mappedBy = "candidate",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<CandidateResume> resumes = new ArrayList<>();

    public void setProfessionalProfile(
        CandidateProfessionalProfile professionalProfile
    ) {
        this.professionalProfile = professionalProfile;

        if (professionalProfile != null) {
            professionalProfile.setCandidate(this);
        }
    }

    public void addWorkExperience(CandidateWorkExperience experience) {
        workExperiences.add(experience);
        experience.setCandidate(this);
    }

    public void addSkill(CandidateSkill skill) {
        skills.add(skill);
        skill.setCandidate(this);
    }

    public void addResume(CandidateResume resume) {
        resumes.add(resume);
        resume.setCandidate(this);
    }
}
