package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.List;

/**
 * Thrown when Phase 1 could not hide every irrelevant job. Phase 2 never
 * starts.
 */
public class JobFilteringException extends IllegalStateException {

    private final transient List<String> failedJobIds;

    public JobFilteringException(List<String> failedJobIds) {
        super("Could not hide all irrelevant jobs. Failed job IDs: "
                + failedJobIds + ". Application phase was not started.");
        this.failedJobIds = List.copyOf(failedJobIds);
    }

    public List<String> getFailedJobIds() {
        return failedJobIds;
    }
}
