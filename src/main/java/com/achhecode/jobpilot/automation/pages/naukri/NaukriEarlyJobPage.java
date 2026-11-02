
package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaukriEarlyJobPage {

    // ============================ SELECTORS ============================

    private static final String EARLY_JOB_URL = "https://www.naukri.com/mnjuser/recommended-earjobs";

    private static final String APPLY_URL_PATTERN = "**/myapply/saveApply**";

    private static final String JOBS = "//div[@class='tuple-list-container']/div[@class='tlc__tuple' and @data-job-id]";

    private static final String SHARE_INTEREST_BUTTON = "xpath=.//button";

    // ============================ TIMEOUTS ============================

    private static final int OPTIONAL_ELEMENT_TIMEOUT_MS = 2000;
    private static final int APPLY_NAVIGATION_TIMEOUT_MS = 10_000;

    private final Page page;

    public NaukriEarlyJobPage(Page page) {
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

    public void navigate() {
        page.navigate(EARLY_JOB_URL);
        waitForJobs();
    }

    public void waitForJobs() {
        page.locator(JOBS).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.ATTACHED));
    }

    // ======================= JOB IDENTIFICATION =======================

    /**
     * Capture all job IDs before clicking Share Interest.
     * Card positions may change after navigation and reload.
     */
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

        log.info("Found {} early job cards", ids.size());

        return ids;
    }

    public Locator findJobById(String jobId) {
        return page.locator(
                "//div[@class='tuple-list-container']" +
                        "/div[@class='tlc__tuple' and @data-job-id="
                        + xpathLiteral(jobId) + "]");
    }

    public boolean jobExists(String jobId) {
        return findJobById(jobId).count() > 0;
    }

    // ======================== SHARE INTEREST ========================

    /**
     * Clicks Share Interest for the specified job.
     *
     * Naukri navigates the SAME tab to /myapply/saveApply.
     *
     * After submission, navigates back to EARLY_JOB_URL
     * so that the next job can be processed.
     *
     * @return true if the expected apply URL contains the job ID.
     */
    public boolean shareInterest(String jobId) {

        // Ensure we are on the early jobs listing page.
        if (!page.url().startsWith(EARLY_JOB_URL)) {
            navigate();
        }

        Locator job = findJobById(jobId);

        if (job.count() == 0) {
            log.warn("Job {} is no longer available", jobId);
            return false;
        }

        boolean clickAttempted = false;
        boolean submitted = false;

        try {

            job.scrollIntoViewIfNeeded();

            Locator button = job.locator(SHARE_INTEREST_BUTTON);

            button.first().waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(OPTIONAL_ELEMENT_TIMEOUT_MS));

            if (!button.first().isEnabled()) {
                log.warn("Share Interest button disabled for job {}", jobId);
                return false;
            }

            // Click Share Interest in the SAME tab.
            clickAttempted = true;
            button.first().click();

            // Wait for navigation to the apply URL.
            page.waitForURL(
                    APPLY_URL_PATTERN,
                    new Page.WaitForURLOptions()
                            .setTimeout(APPLY_NAVIGATION_TIMEOUT_MS));

            // Verify that the expected job ID is present.
            submitted = isApplyUrlForJob(jobId);

            if (submitted) {
                log.info(
                        "Share Interest submitted successfully for job {}",
                        jobId);
            } else {
                log.warn(
                        "Unexpected apply URL for job {}: {}",
                        jobId,
                        page.url());
            }

        } catch (TimeoutError e) {

            // Navigation may have completed even if a wait timed out.
            submitted = isApplyUrlForJob(jobId);

            log.warn(
                    "Apply navigation timeout for job {}. URL: {}. Error: {}",
                    jobId,
                    page.url(),
                    firstLine(e));

        } catch (Exception e) {

            log.error("Failed to share interest for job {}", jobId, e);

        } finally {

            // Return to the early jobs listing before processing next job.
            if (clickAttempted) {
                returnToEarlyJobsPage(jobId);
            }
        }

        return submitted;
    }

    /**
     * Validates that the current URL belongs to the expected job submission.
     */
    private boolean isApplyUrlForJob(String jobId) {

        String currentUrl = page.url();

        return currentUrl.contains("/myapply/saveApply")
                && currentUrl.contains(jobId);
    }

    /**
     * Navigates back to the early jobs listing after each attempt.
     */
    private void returnToEarlyJobsPage(String jobId) {

        try {

            if (!page.url().startsWith(EARLY_JOB_URL)) {

                log.info(
                        "Returning to early jobs listing after processing job {}",
                        jobId);

                navigate();
            } else {
                waitForJobs();
            }

            log.info("Early jobs listing loaded again");

        } catch (Exception e) {

            log.error(
                    "Failed to return to early jobs listing after job {}: {}",
                    jobId,
                    firstLine(e));
        }
    }

    // ======================== BULK PROCESSING ========================

    /**
     * Processes early jobs up to the specified maximum.
     *
     * maxJobsToProcess:
     * 0 = Process all available jobs.
     * >0 = Process at most the specified number of jobs.
     * <0 = Throws IllegalArgumentException.
     *
     * @return number of successfully submitted jobs.
     */
    public int shareInterestForAllJobs(int maxJobsToProcess) {

        if (maxJobsToProcess < 0) {
            throw new IllegalArgumentException(
                    "maxJobsToProcess cannot be negative: "
                            + maxJobsToProcess);
        }

        List<String> jobIds = getJobIds();

        int successCount = 0;
        int processedCount = 0;

        int totalToProcess = maxJobsToProcess == 0
                ? jobIds.size()
                : Math.min(maxJobsToProcess, jobIds.size());

        for (String jobId : jobIds) {

            if (processedCount >= totalToProcess) {
                break;
            }

            processedCount++;

            log.info(
                    "Processing early job {}/{} : {}",
                    processedCount,
                    totalToProcess,
                    jobId);

            if (shareInterest(jobId)) {
                successCount++;
            }
        }

        log.info(
                "Early jobs processing completed. Success: {}, Processed: {}, Available: {}",
                successCount,
                processedCount,
                jobIds.size());

        return successCount;
    }

    // ========================= INTERNALS =========================

    private String firstLine(Exception e) {

        String msg = e.getMessage();

        return msg == null
                ? e.getClass().getSimpleName()
                : msg.lines().findFirst().orElse(msg);
    }

    /**
     * Safely converts a value to an XPath string literal.
     */
    private String xpathLiteral(String value) {

        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        return "concat('" +
                String.join("', \"'\", '", value.split("'", -1)) +
                "')";
    }
}
