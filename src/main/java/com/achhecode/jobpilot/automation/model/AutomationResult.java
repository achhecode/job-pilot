package com.achhecode.jobpilot.automation.model;

public record AutomationResult(Status status, String message, String jobUrl) {
    public enum Status {
        SUBMITTED,
        REQUIRES_REVIEW,
        FAILED
    }

    public static AutomationResult submitted(String message, String jobUrl) {
        return new AutomationResult(Status.SUBMITTED, message, jobUrl);
    }

    public static AutomationResult requiresReview(String message, String jobUrl) {
        return new AutomationResult(Status.REQUIRES_REVIEW, message, jobUrl);
    }

    public static AutomationResult failed(String message, String jobUrl) {
        return new AutomationResult(Status.FAILED, message, jobUrl);
    }
}
