package com.achhecode.jobpilot.candidate.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.achhecode.jobpilot.candidate.dto.CandidateCreateRequest;
import com.achhecode.jobpilot.candidate.dto.CandidateCreateResponse;
import com.achhecode.jobpilot.candidate.dto.CandidateProfessionalRequest;
import com.achhecode.jobpilot.candidate.dto.CandidateSkillRequest;
import com.achhecode.jobpilot.candidate.dto.CandidateWorkExperienceRequest;
import com.achhecode.jobpilot.candidate.entity.CandidateProfessionalProfile;
import com.achhecode.jobpilot.candidate.entity.CandidateProfile;
import com.achhecode.jobpilot.candidate.entity.CandidateSkill;
import com.achhecode.jobpilot.candidate.entity.CandidateWorkExperience;
import com.achhecode.jobpilot.candidate.repository.CandidateProfessionalProfileRepository;
import com.achhecode.jobpilot.candidate.repository.CandidateProfileRepository;
import com.achhecode.jobpilot.candidate.repository.CandidateSkillRepository;
import com.achhecode.jobpilot.candidate.repository.CandidateWorkExperienceRepository;
import com.achhecode.jobpilot.common.enums.NoticePeriodType;
import com.achhecode.jobpilot.common.enums.ProfileStatus;
import com.achhecode.jobpilot.skill.entity.Skill;
import com.achhecode.jobpilot.skill.repository.SkillRepository;

@Service
public class CandidateService {

    private final CandidateProfileRepository candidateRepository;
    private final CandidateProfessionalProfileRepository professionalRepository;
    private final CandidateWorkExperienceRepository experienceRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final SkillRepository skillRepository;

    public CandidateService(
            CandidateProfileRepository candidateRepository,
            CandidateProfessionalProfileRepository professionalRepository,
            CandidateWorkExperienceRepository experienceRepository,
            CandidateSkillRepository candidateSkillRepository,
            SkillRepository skillRepository) {
        this.candidateRepository = candidateRepository;
        this.professionalRepository = professionalRepository;
        this.experienceRepository = experienceRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public CandidateCreateResponse createCandidate(
            CandidateCreateRequest request) {
        if (candidateRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists: " + request.username());
        }

        CandidateProfile candidate = new CandidateProfile();
        candidate.setUsername(request.username());
        candidate.setFullName(request.fullName());
        candidate.setEmail(request.email());
        candidate.setPhoneNumber(request.phoneNumber());
        candidate.setGender(request.gender());
        candidate.setDateOfBirth(request.dateOfBirth());
        candidate.setCountryCode(request.countryCode());
        candidate.setStateRegion(request.stateRegion());
        candidate.setCity(request.city());
        candidate.setPostalCode(request.postalCode());
        candidate.setAddressLine1(request.addressLine1());
        candidate.setAddressLine2(request.addressLine2());
        candidate.setProfessionalSummary(request.professionalSummary());
        candidate.setLinkedinUrl(request.linkedinUrl());
        candidate.setGithubUrl(request.githubUrl());
        candidate.setPortfolioUrl(request.portfolioUrl());
        candidate.setProfileStatus(
                request.profileStatus() != null
                        ? request.profileStatus()
                        : ProfileStatus.ACTIVE);

        candidateRepository.save(candidate);

        if (request.professionalProfile() != null) {
            CandidateProfessionalProfile profile = toProfessionalProfile(candidate, request.professionalProfile());

            professionalRepository.save(profile);
        }

        if (request.workExperiences() != null) {
            for (CandidateWorkExperienceRequest item : request.workExperiences()) {
                CandidateWorkExperience experience = toWorkExperience(candidate, item);

                experienceRepository.save(experience);
            }
        }

        if (request.skills() != null) {
            List<Long> skillIds = request.skills().stream()
                    .filter(Objects::nonNull)
                    .map(skill -> skill.skillId())
                    .toList();

            if (skillIds.stream().distinct().count() != skillIds.size()) {
                throw new IllegalArgumentException(
                        "Duplicate skillId values are not allowed.");
            }

            for (CandidateSkillRequest item : request.skills()) {
                Skill skill = skillRepository.findById(item.skillId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Skill not found: " + item.skillId()));

                CandidateSkill candidateSkill = new CandidateSkill();
                candidateSkill.setCandidate(candidate);
                candidateSkill.setSkill(skill);
                candidateSkill.setExperienceMonths(item.experienceMonths());
                candidateSkill.setProficiencyLevel(item.proficiencyLevel());
                candidateSkill.setLastUsedDate(item.lastUsedDate());
                candidateSkill.setPrimarySkill(
                        Boolean.TRUE.equals(item.primarySkill()));
                candidateSkillRepository.save(candidateSkill);
            }
        }

        return new CandidateCreateResponse(candidate.getId(), candidate.getUsername(), candidate.getFullName(),
                "Candidate created successfully");

    }

    private CandidateProfessionalProfile toProfessionalProfile(
            CandidateProfile candidate,
            CandidateProfessionalRequest request) {
        CandidateProfessionalProfile profile = new CandidateProfessionalProfile();

        profile.setCandidate(candidate);
        profile.setTotalExperienceMonths(request.totalExperienceMonths());
        profile.setNoticePeriodDays(request.noticePeriodDays());
        profile.setNoticePeriodType(
                request.noticePeriodType() != null
                        ? request.noticePeriodType()
                        : NoticePeriodType.FIXED);
        profile.setCurrentCtc(request.currentCtc());
        profile.setExpectedCtc(request.expectedCtc());
        profile.setCurrencyCode(request.currencyCode());
        profile.setEmploymentStatus(
                request.employmentStatus() == null
                        ? null
                        : request.employmentStatus());
        profile.setPreferredWorkMode(request.preferredWorkMode());
        profile.setWillingToRelocate(
                Boolean.TRUE.equals(request.willingToRelocate()));

        return profile;
    }

    private CandidateWorkExperience toWorkExperience(
            CandidateProfile candidate,
            CandidateWorkExperienceRequest request) {
        boolean current = Boolean.TRUE.equals(request.current());

        if (request.endDate() != null &&
                request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "Work experience endDate cannot be before startDate.");
        }

        if (current && request.endDate() != null) {
            throw new IllegalArgumentException(
                    "Current work experience must not have an endDate.");
        }

        CandidateWorkExperience experience = new CandidateWorkExperience();

        experience.setCandidate(candidate);
        experience.setCompanyName(request.companyName());
        experience.setJobTitle(request.jobTitle());
        experience.setEmploymentType(request.employmentType());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCurrent(current);
        experience.setDescription(request.description());
        experience.setLocation(request.location());

        return experience;
    }
}
