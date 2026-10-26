package com.achhecode.jobpilot.application.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyId;

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

    private Instant updatedAt = Instant.now();

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    protected JobApplication() {}

    public JobApplication(String companyId, String jobUrl, String jobTitle) {
        this.companyId = companyId;
        this.jobUrl = jobUrl;
        this.jobTitle = jobTitle;
    }

    public Long getId() { return id; }
    public String getCompanyId() { return companyId; }
    public String getJobUrl() { return jobUrl; }
    public String getJobTitle() { return jobTitle; }
    public ApplicationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(ApplicationStatus status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
}
