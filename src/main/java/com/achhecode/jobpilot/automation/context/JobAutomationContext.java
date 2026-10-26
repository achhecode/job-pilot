package com.achhecode.jobpilot.automation.context;

import com.microsoft.playwright.Page;
import java.util.Objects;

/**
 * Represents one automation attempt.
 *
 * The page must be created by achhecode-playwright. This class does not create,
 * own, or close the Browser or BrowserContext.
 */
public final class JobAutomationContext {
    private final Page page;

    public JobAutomationContext(Page page) {
        this.page = Objects.requireNonNull(page, "page");
    }

    public Page page() {
        return page;
    }
}
