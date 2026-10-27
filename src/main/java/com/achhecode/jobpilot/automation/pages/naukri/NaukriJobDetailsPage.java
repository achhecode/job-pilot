package com.achhecode.jobpilot.automation.pages.naukri;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

/**
 * Page object for the job-details TAB that opens after clicking a job card.
 * It wraps its OWN Playwright Page, so every locator below is resolved from
 * the details page root, never from the listing page or a job card.
 */
@Slf4j
public class NaukriJobDetailsPage {

    // All XPaths are absolute (page-rooted) and explicitly prefixed with "xpath=".
    private static final String APPLY_BUTTON = "xpath=//section[@id='job_header']//button[@id='apply-button']";
    private static final String COMPANY_SITE_BUTTON = "xpath=//section[@id='job_header']//button[@id='company-site-button']";
    private static final String ALREADY_APPLIED = "xpath=//section[@id='job_header']//*[@id='already-applied']";

    // Any one of the three action buttons means the header has rendered.
    private static final String ANY_ACTION_BUTTON = APPLY_BUTTON
            + " | " + COMPANY_SITE_BUTTON.substring("xpath=".length())
            + " | " + ALREADY_APPLIED.substring("xpath=".length());

    private static final String APPLY_SUCCESS = "text=/applied to|successfully applied|application (has been )?(sent|submitted)/i";
    private static final String QUESTIONNAIRE = "css=[class*='chatbot_Drawer'], [class*='chatbot_Container']";

    private static final int HEADER_TIMEOUT_MS = 5000;
    private static final int CLICK_TIMEOUT_MS = 3000;
    private static final int OUTCOME_TIMEOUT_MS = 6000;
    private static final int POLL_INTERVAL_MS = 250;

    private final Page page;

    public NaukriJobDetailsPage(Page page) {
        this.page = page;
    }

    public String getUrl() {
        return page.url();
    }

    public boolean isClosed() {
        return page.isClosed();
    }

    public void waitUntilLoaded() {
        try {
            page.waitForLoadState();
        } catch (TimeoutError e) {
            log.warn("Load-state wait timed out for {}; continuing", page.url());
        }
    }

    /** Clicks Apply on this tab and reports the VERIFIED outcome. */
    public ApplyResult apply() {
        if (page.isClosed()) {
            return ApplyResult.JOB_UNAVAILABLE;
        }

        waitForHeader();

        if (isPresent(ALREADY_APPLIED)) {
            return ApplyResult.ALREADY_APPLIED;
        }
        if (!isPresent(APPLY_BUTTON)) {
            return isPresent(COMPANY_SITE_BUTTON)
                    ? ApplyResult.EXTERNAL_SITE
                    : ApplyResult.BUTTON_UNAVAILABLE;
        }

        Locator applyButton = page.locator(APPLY_BUTTON).first();

        try {
            applyButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(CLICK_TIMEOUT_MS));

            if (!applyButton.isEnabled()) {
                log.warn("Apply button disabled on {}", page.url());
                return ApplyResult.BUTTON_UNAVAILABLE;
            }
            applyButton.click();

        } catch (Exception e) {
            log.warn("Apply click failed on {}: {}", page.url(), e.getMessage());
            return ApplyResult.BUTTON_UNAVAILABLE;
        }

        return waitForOutcome();
    }

    public void close() {
        if (page.isClosed()) {
            return;
        }
        try {
            page.close();
        } catch (Exception e) {
            log.warn("Could not close details tab: {}", e.getMessage());
        }
    }

    // ------------------------- internals -------------------------

    /**
     * Wait until Apply / Company-site / Already-applied is attached, so we don't
     * read a half-rendered page.
     */
    private void waitForHeader() {
        try {
            page.locator(ANY_ACTION_BUTTON).first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(HEADER_TIMEOUT_MS));
        } catch (TimeoutError e) {
            log.warn("No action button rendered on {}", page.url());
        }
    }

    private ApplyResult waitForOutcome() {
        long deadline = System.currentTimeMillis() + OUTCOME_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline && !page.isClosed()) {
            if (isPresent(QUESTIONNAIRE)) {
                return ApplyResult.QUESTIONNAIRE_REQUIRED;
            }
            if (isPresent(ALREADY_APPLIED) || isPresent(APPLY_SUCCESS)) {
                return ApplyResult.APPLIED;
            }
            page.waitForTimeout(POLL_INTERVAL_MS);
        }
        return ApplyResult.UNKNOWN;
    }

    private boolean isPresent(String selector) {
        try {
            return page.locator(selector).count() > 0;
        } catch (Exception e) {
            return false; // tab navigated or closed mid-check
        }
    }
}
