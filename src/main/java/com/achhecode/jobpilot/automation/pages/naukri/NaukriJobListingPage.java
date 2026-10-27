package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

/**
 * Page object: knows WHERE things are and HOW to click them.
 * It contains no filtering or workflow decisions (see NaukriJobProcessor).
 */
@Slf4j
public class NaukriJobListingPage {

    // ---------- Listing page selectors ----------
    private static final String JOBS = "//article[@data-job-id]";
    private static final String TITLE = "xpath=.//a[contains(@class, 'title')]";
    private static final String EXPERIENCE = "xpath=.//ul[contains(@class, 'job-desc')]//li[contains(@class, 'experience')]";
    private static final String SALARY = "xpath=.//ul[contains(@class, 'job-desc')]//li[contains(@class, 'salary')]";
    private static final String TAGS = "xpath=.//ul[contains(@class, 'tags')]//li[contains(@class, 'dot')]";
    private static final String POSTED = "xpath=.//div[contains(@class, 'jobTupleFooter')]/div/span";
    private static final String HIDE_BUTTON = "xpath=.//button[contains(@class, 'jt-btn-reset')][.//i[contains(@class, 'hide')]]";

    // ---------- Job details page selectors (adjust if Naukri changes its DOM)
    // ----------
    private static final String APPLY_BUTTON = "//section[@id='job_header']//button[@id='apply-button']";
    private static final String COMPANY_SITE_BUTTON = "//section[@id='job_header']//button[@id='company-site-button']";
    private static final String ALREADY_APPLIED = "//section[@id='job_header']//*[@id='already-applied']";
    private static final String APPLY_SUCCESS = "text=/applied to|successfully applied|application (has been )?(sent|submitted)/i";
    private static final String QUESTIONNAIRE = "[class*='chatbot_Drawer'], [class*='chatbot_Container']";

    private static final int OPTIONAL_ELEMENT_TIMEOUT_MS = 2000;
    private static final int HIDE_CONFIRM_TIMEOUT_MS = 3000;
    private static final int APPLY_OUTCOME_TIMEOUT_MS = 6000;
    private static final int POLL_INTERVAL_MS = 250;

    private final Page page;

    public NaukriJobListingPage(Page page) {
        this.page = page;
    }

    // ============================ SETUP ============================

    public void configureTimeouts() {
        page.setDefaultTimeout(3000);
        page.setDefaultNavigationTimeout(10_000);
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void waitForJobs() {
        page.locator(JOBS).first().waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    }

    // ======================= JOB IDENTIFICATION =======================

    /** Capture IDs BEFORE hiding anything; card indexes shift afterwards. */
    public List<String> getJobIds() {
        Locator jobs = page.locator(JOBS);
        List<String> ids = new ArrayList<>();
        int count = jobs.count();

        for (int i = 0; i < count; i++) {
            String id = jobs.nth(i).getAttribute("data-job-id");
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    public Locator findJobById(String jobId) {
        return page.locator(JOBS + "[@data-job-id=" + xpathLiteral(jobId) + "]");
    }

    public boolean jobExists(String jobId) {
        return findJobById(jobId).count() > 0;
    }

    // ======================== DATA EXTRACTION ========================

    /** @return the card snapshot, or null if the card no longer exists. */
    public JobCard readJobCard(String jobId) {
        Locator job = findJobById(jobId);
        if (job.count() == 0) {
            return null;
        }
        return new JobCard(
                jobId,
                text(job.locator(TITLE)),
                text(job.locator(EXPERIENCE)),
                text(job.locator(SALARY)),
                allTexts(job.locator(TAGS)),
                text(job.locator(POSTED)));
    }

    // =========================== HIDING ===========================

    /**
     * Clicks Hide and then confirms the card actually left the page.
     * Returns true only when the card is gone/hidden.
     */
    public boolean hideJob(String jobId) {
        Locator job = findJobById(jobId);
        if (job.count() == 0) {
            log.info("Job {} is already gone, nothing to hide", jobId);
            return true;
        }

        try {
            job.scrollIntoViewIfNeeded();
            job.hover(); // the hide icon is often rendered only on hover

            Locator hideButton = job.locator(HIDE_BUTTON);
            hideButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(OPTIONAL_ELEMENT_TIMEOUT_MS));

            if (!hideButton.isEnabled()) {
                log.warn("Hide button is disabled for job {}", jobId);
                return false;
            }

            hideButton.click();

            // Confirm: the card must become hidden or detached.
            job.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(HIDE_CONFIRM_TIMEOUT_MS));

            log.info("Job {} hidden and confirmed", jobId);
            return true;

        } catch (TimeoutError e) {
            log.warn("Hide not confirmed for job {}: {}", jobId, firstLine(e));
            return false;
        } catch (Exception e) {
            log.error("Failed to hide job {}", jobId, e);
            return false;
        }
    }

    // ======================== JOB DETAILS TAB ========================

    /**
     * Opens the job in a new tab. Caller must close the returned page. Returns null
     * on failure.
     */
    public Page openJobDetails(String jobId) {
        Locator job = findJobById(jobId);
        if (job.count() == 0) {
            log.warn("Cannot open missing job {}", jobId);
            return null;
        }

        Locator title = job.locator(TITLE);
        Locator target = title.count() > 0 ? title.first() : job;

        try {
            Page details = page.context().waitForPage(target::click);
            try {
                details.waitForLoadState();
            } catch (TimeoutError e) {
                log.warn("Load-state wait timed out for job {}; continuing", jobId);
            }
            return details;
        } catch (Exception e) {
            log.warn("Could not open details for job {}: {}", jobId, firstLine(e));
            return null;
        }
    }

    /** Clicks Apply on the details page and reports the VERIFIED outcome. */
    public ApplyResult apply(Page details) {
        if (details == null || details.isClosed()) {
            return ApplyResult.JOB_UNAVAILABLE;
        }

        // Pre-click checks
        if (isPresent(details, ALREADY_APPLIED)) {
            return ApplyResult.ALREADY_APPLIED;
        }
        Locator applyButton = details.locator(APPLY_BUTTON);

        if (!isPresent(details, APPLY_BUTTON)) {
            return isPresent(details, COMPANY_SITE_BUTTON)
                    ? ApplyResult.EXTERNAL_SITE
                    : ApplyResult.BUTTON_UNAVAILABLE;
        }

        try {
            if (!applyButton.isEnabled()) {
                log.warn("Apply button disabled on {}", details.url());
                return ApplyResult.BUTTON_UNAVAILABLE;
            }
            applyButton.click();
        } catch (Exception e) {
            log.warn("Apply click failed on {}: {}", details.url(), firstLine(e));
            return ApplyResult.BUTTON_UNAVAILABLE;
        }

        return waitForApplyOutcome(details);
    }

    public void closeTab(Page details) {
        if (details == null || details.isClosed()) {
            return;
        }
        try {
            details.close();
        } catch (Exception e) {
            log.warn("Could not close tab: {}", e.getMessage());
        }
    }

    // ========================= INTERNALS =========================

    private ApplyResult waitForApplyOutcome(Page details) {
        long deadline = System.currentTimeMillis() + APPLY_OUTCOME_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline && !details.isClosed()) {
            if (isPresent(details, QUESTIONNAIRE)) {
                return ApplyResult.QUESTIONNAIRE_REQUIRED;
            }
            if (isPresent(details, ALREADY_APPLIED) || isPresent(details, APPLY_SUCCESS)) {
                return ApplyResult.APPLIED;
            }
            details.waitForTimeout(POLL_INTERVAL_MS);
        }
        return ApplyResult.UNKNOWN;
    }

    private boolean isPresent(Page p, String selector) {
        try {
            return p.locator(selector).count() > 0;
        } catch (Exception e) {
            return false; // page navigated/closed mid-check
        }
    }

    private String text(Locator locator) {
        try {
            return locator.count() == 0 ? "" : locator.first().innerText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> allTexts(Locator locator) {
        try {
            return locator.allInnerTexts().stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String firstLine(Exception e) {
        String msg = e.getMessage();
        return msg == null ? e.getClass().getSimpleName() : msg.lines().findFirst().orElse(msg);
    }

    /** Safely converts a value to an XPath string literal (handles ' and "). */
    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + String.join("', \"'\", '", value.split("'", -1)) + "')";
    }
}
