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

    private static final int OPTIONAL_ELEMENT_TIMEOUT_MS = 2000;
    private static final int HIDE_CONFIRM_TIMEOUT_MS = 3000;

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

    // ======================== OPEN DETAILS TAB ========================

    /**
     * Clicks the job title; Naukri opens the job in a NEW TAB, which is wrapped
     * in its own page object. The caller must close it (details.close()).
     * Returns null when the job is missing or the tab could not be opened.
     */
    public NaukriJobDetailsPage openJobDetails(String jobId) {
        Locator job = findJobById(jobId);
        if (job.count() == 0) {
            log.warn("Cannot open missing job {}", jobId);
            return null;
        }

        Locator title = job.locator(TITLE);
        Locator target = title.count() > 0 ? title.first() : job;

        try {
            Page newTab = page.context().waitForPage(target::click);
            NaukriJobDetailsPage details = new NaukriJobDetailsPage(newTab);
            details.waitUntilLoaded();
            return details;
        } catch (Exception e) {
            log.warn("Could not open details for job {}: {}", jobId, firstLine(e));
            return null;
        }
    }

    // ========================= INTERNALS =========================

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
