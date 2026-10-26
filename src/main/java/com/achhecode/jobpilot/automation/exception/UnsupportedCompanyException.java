package com.achhecode.jobpilot.automation.exception;

public class UnsupportedCompanyException extends RuntimeException {
    public UnsupportedCompanyException(String companyId) {
        super("No job application strategy registered for company: " + companyId);
    }
}
