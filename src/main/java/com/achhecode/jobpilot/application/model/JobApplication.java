package com.achhecode.jobpilot.application.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateUsername;

    @Column(nullable = false)
    private String jobId;

    @Column(nullable = false, length = 2048)
    private String jobUrl;

    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(length = 2000)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected JobApplication() {
    }

    public JobApplication(
            String candidateUsername,
            String jobId,
            String jobUrl,
            String jobTitle,
            ApplicationStatus status,
            String message) {
        this.candidateUsername = candidateUsername;
        this.jobId = jobId;
        this.jobUrl = jobUrl;
        this.jobTitle = jobTitle;
        this.status = status;
        this.message = message;
    }

    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}
