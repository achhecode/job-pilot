package com.achhecode.jobpilot.automation.pages.naukri;

/** Verified outcome of trying to apply to one job. */
public enum ApplyResult {
    /** Success confirmation detected. */
    APPLIED,
    /** Naukri opened a chatbot/questionnaire; needs manual answers. */
    QUESTIONNAIRE_REQUIRED,
    /** Job only accepts applications on the company website. */
    EXTERNAL_SITE,
    /** Job was already applied to earlier. */
    ALREADY_APPLIED,
    /** Job disappeared from the listing or the tab could not be opened. */
    JOB_UNAVAILABLE,
    /** No usable Apply button on the details page. */
    BUTTON_UNAVAILABLE,
    /** Apply was clicked but no known outcome was detected. */
    UNKNOWN,
    /** Unexpected exception. */
    ERROR;

    /** Tabs for these outcomes need a human, so they are left open. */
    public boolean needsManualReview() {
        return this == QUESTIONNAIRE_REQUIRED || this == UNKNOWN;
    }
}
