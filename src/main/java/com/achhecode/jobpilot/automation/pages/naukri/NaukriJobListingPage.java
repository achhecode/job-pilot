
package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaukriJobListingPage {

    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private static final String JOBS_XPATH = "//article[@data-job-id]";

    private static final String EXPERIENCE_XPATH = "xpath=.//div[contains(@class, 'info')]"
            + "//ul[contains(@class, 'job-desc')]"
            + "//li[contains(@class, 'experience')]";

    private static final String SALARY_XPATH = "xpath=.//div[contains(@class, 'info')]"
            + "//ul[contains(@class, 'job-desc')]"
            + "//li[contains(@class, 'salary')]";

    private static final String HIDE_BUTTON_XPATH = "xpath=.//button[contains(@class, 'jt-btn-reset')]"
            + "[.//i[contains(@class, 'hide')]]";

    private static final String APPLY_BUTTON_XPATH = "//section[@id='job_header']"
            + "//button[@id='apply-button']";

    private static final int OPTIONAL_ELEMENT_TIMEOUT_MS = 2000;

    private final Page page;

    public NaukriJobListingPage(Page page) {
        this.page = page;
    }

    /**
     * Configures timeouts for this page.
     */
    public void configureTimeouts() {
        page.setDefaultTimeout(3000);
        page.setDefaultNavigationTimeout(10000);
    }

    /**
     * Returns the current listing page URL.
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Returns the number of job cards currently in the listing.
     */
    public int getJobCount() {
        return page.locator(JOBS_XPATH).count();
    }

    /**
     * Waits until at least one job card is attached to the DOM.
     */
    public void waitForJobs() {
        page.locator(JOBS_XPATH)
                .first()
                .waitFor(
                        new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.ATTACHED));
    }

    /**
     * Captures stable job IDs before any listing modifications.
     *
     * This avoids relying on card indexes after jobs are hidden.
     */
    public List<String> getJobIds() {
        Locator jobs = page.locator(JOBS_XPATH);
        List<String> jobIds = new ArrayList<>();

        int count = jobs.count();

        for (int i = 0; i < count; i++) {
            String jobId = jobs.nth(i).getAttribute("data-job-id");

            if (jobId != null && !jobId.isBlank()) {
                jobIds.add(jobId);
            }
        }

        return jobIds;
    }

    /**
     * Finds a job card by its stable data-job-id.
     */
    public Locator findJobById(String jobId) {
        return page.locator(
                JOBS_XPATH
                        + "[@data-job-id="
                        + xpathLiteral(jobId)
                        + "]");
    }

    /**
     * Checks whether a job card currently exists.
     */
    public boolean jobExists(String jobId) {
        return findJobById(jobId).count() > 0;
    }

    /**
     * Extracts experience text from a job card.
     *
     * Example: "4-8 Yrs"
     */
    public String getExperience(String jobId) {
        Locator job = findJobById(jobId);
        return getText(job.locator(EXPERIENCE_XPATH));
    }

    /**
     * Extracts salary text from a job card.
     *
     * Example: "5-12 Lacs PA"
     */
    public String getSalary(String jobId) {
        Locator job = findJobById(jobId);
        return getText(job.locator(SALARY_XPATH));
    }

    /**
     * Extracts the maximum number from a numeric range or value.
     *
     * "4-8 Yrs" -> 8
     * "5-12 Lacs PA" -> 12
     * "6 Yrs" -> 6
     * "Not disclosed" -> -1
     */
    public int getMaximumValue(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }

        Matcher rangeMatcher = RANGE_PATTERN.matcher(text);

        if (rangeMatcher.find()) {
            return Integer.parseInt(rangeMatcher.group(2));
        }

        Matcher numberMatcher = NUMBER_PATTERN.matcher(text);

        if (numberMatcher.find()) {
            return Integer.parseInt(numberMatcher.group());
        }

        return -1;
    }

    /**
     * Checks whether the salary is undisclosed.
     */
    public boolean isSalaryUndisclosed(String salary) {
        return salary != null
                && salary.toLowerCase(Locale.ROOT)
                        .contains("not disclosed");
    }

    /**
     * Determines whether a job satisfies the configured filters.
     *
     * Experience must meet the minimum.
     * Undisclosed salary is allowed.
     * Disclosed salary must be strictly greater than minSalaryLpa.
     */
    public boolean matchesFilters(
            String jobId,
            int minExperience,
            int minSalaryLpa) {

        String experience = getExperience(jobId);
        String salary = getSalary(jobId);

        int maximumExperience = getMaximumValue(experience);

        boolean experienceMatches = maximumExperience >= minExperience;

        boolean salaryMatches;

        if (isSalaryUndisclosed(salary)) {
            salaryMatches = true;
        } else {
            int maximumSalary = getMaximumValue(salary);
            salaryMatches = maximumSalary > minSalaryLpa;
        }

        return experienceMatches && salaryMatches;
    }

    /**
     * Hides a job that does not match the configured filters.
     *
     * Returns true when the hide button was clicked.
     */
    public boolean hideJob(String jobId) {
        Locator job = findJobById(jobId);

        if (job.count() == 0) {
            return false;
        }

        Locator hideButton = job.locator(HIDE_BUTTON_XPATH);

        try {
            hideButton.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(OPTIONAL_ELEMENT_TIMEOUT_MS));

            if (hideButton.isVisible() && hideButton.isEnabled()) {
                hideButton.click();
                return true;
            }

        } catch (TimeoutError e) {
            log.info(
                    "Hide button unavailable for job: " + jobId);
        }

        return false;
    }

    /**
     * Opens a job card in a new tab and returns that tab.
     *
     * The caller is responsible for closing the returned page.
     */
    public Page openJobDetails(String jobId) {
        Locator job = findJobById(jobId);

        if (job.count() == 0) {
            return null;
        }

        BrowserContext context = page.context();

        return context.waitForPage(
                () -> job.click());
    }

    /**
     * Clicks Apply on a job details page.
     *
     * Returns true when the button was clicked.
     */
    public boolean clickApply(Page jobDetailsPage) {
        if (jobDetailsPage == null || jobDetailsPage.isClosed()) {
            return false;
        }

        Locator applyButton = jobDetailsPage.locator(APPLY_BUTTON_XPATH);

        try {
            applyButton.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(OPTIONAL_ELEMENT_TIMEOUT_MS));

            if (applyButton.isVisible() && applyButton.isEnabled()) {
                applyButton.click();
                return true;
            }

        } catch (TimeoutError e) {
            log.info(
                    "Apply button unavailable on: "
                            + jobDetailsPage.url());
        }

        return false;
    }

    /**
     * Closes a job details tab safely.
     */
    public void closeJobDetails(Page jobDetailsPage) {
        if (jobDetailsPage == null || jobDetailsPage.isClosed()) {
            return;
        }

        try {
            jobDetailsPage.close();
        } catch (Exception e) {
            System.err.println(
                    "Could not close job details tab: "
                            + e.getMessage());
        }
    }

    /**
     * Processes a maximum number of jobs using the supplied filters.
     *
     * Captures job IDs first because hiding a card can change the listing.
     */
    public JobProcessingSummary processJobs(
            int minExperience,
            int minSalaryLpa,
            int maxCap) {

        if (minExperience < 0 || minSalaryLpa < 0 || maxCap < 0) {
            throw new IllegalArgumentException(
                    "Experience, salary and job limit cannot be negative.");
        }

        waitForJobs();

        List<String> jobIds = getJobIds();

        int totalJobs = jobIds.size();
        int limit = Math.min(maxCap, totalJobs);

        int matchedJobs = 0;
        int hiddenJobs = 0;
        int openedJobs = 0;
        int skippedJobs = 0;

        log.info("Current URL: " + getCurrentUrl());
        log.info("Total jobs found: " + totalJobs);
        log.info("Jobs to inspect: " + limit);

        for (int i = 0; i < limit; i++) {
            String jobId = jobIds.get(i);
            Page jobDetailsPage = null;

            try {
                log.info(
                        "{}========== Job {} | ID: {} ==========",
                        i + 1, limit, jobId);

                if (!jobExists(jobId)) {
                    log.info(
                            "Job no longer exists. Skipping.");
                    skippedJobs++;
                    continue;
                }

                String experience = getExperience(jobId);
                String salary = getSalary(jobId);

                int maximumExperience = getMaximumValue(experience);

                int maximumSalary = isSalaryUndisclosed(salary)
                        ? -1
                        : getMaximumValue(salary);

                log.info(
                        "Experience: {} | Maximum: {}",
                        experience, maximumExperience);

                log.info(
                        "Salary: {} | Maximum LPA: {}",
                        salary,
                        isSalaryUndisclosed(salary)
                                ? "Undisclosed"
                                : String.valueOf(maximumSalary));

                if (!matchesFilters(
                        jobId, minExperience, minSalaryLpa)) {

                    log.info(
                            "Does not match filters. Hiding job.");

                    if (hideJob(jobId)) {
                        hiddenJobs++;
                        log.info(
                                "Hide action clicked for job: " + jobId);
                    } else {
                        log.info(
                                "Could not hide job: " + jobId);
                    }

                    continue;
                }

                matchedJobs++;

                log.info(
                        "MATCHED: Opening job " + jobId);

                jobDetailsPage = openJobDetails(jobId);

                if (jobDetailsPage == null) {
                    log.info(
                            "Could not open job: " + jobId);
                    skippedJobs++;
                    continue;
                }

                openedJobs++;

                log.info(
                        "Opened details: " + jobDetailsPage.url());

                try {
                    jobDetailsPage.waitForLoadState();
                } catch (TimeoutError e) {
                    log.info(
                            "Load-state wait timed out; checking Apply anyway.");
                }

                boolean applyClicked = clickApply(jobDetailsPage);

                if (applyClicked) {
                    log.info(
                            "Apply button clicked for job: " + jobId);
                } else {
                    log.info(
                            "Apply button unavailable. Skipping.");
                    skippedJobs++;
                }

            } catch (TimeoutError e) {
                skippedJobs++;

                System.err.printf(
                        "Timeout processing job ID %s: %s%n",
                        jobId, e.getMessage());

            } catch (Exception e) {
                skippedJobs++;

                System.err.printf(
                        "Error processing job ID %s: %s%n",
                        jobId, e.getMessage());

            } finally {
                closeJobDetails(jobDetailsPage);
            }
        }

        JobProcessingSummary summary = new JobProcessingSummary(
                totalJobs,
                limit,
                matchedJobs,
                hiddenJobs,
                openedJobs,
                skippedJobs);

        printSummary(summary);

        return summary;
    }

    /**
     * Safely extracts text from a locator.
     */
    private String getText(Locator locator) {
        if (locator.count() == 0) {
            return "";
        }

        return locator.first().innerText().trim();
    }

    /**
     * Safely converts a string into an XPath string literal.
     * Handles IDs containing single or double quotes.
     */
    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'", -1);

        return "concat('"
                + String.join("', \"'\", '", parts)
                + "')";
    }

    /**
     * Prints the final processing summary.
     */
    private void printSummary(JobProcessingSummary summary) {
        log.info("\n========== SUMMARY ==========");
        log.info("Total jobs found: " + summary.getTotalJobs());
        log.info("Jobs inspected: " + summary.getInspectedJobs());
        log.info("Matching jobs: " + summary.getMatchedJobs());
        log.info("Hide actions clicked: " + summary.getHiddenJobs());
        log.info("Job detail pages opened: " + summary.getOpenedJobs());
        log.info("Jobs skipped/errors: " + summary.getSkippedJobs());
        log.info("Finished processing.");
    }

    /**
     * Holds the final statistics.
     */
    public static class JobProcessingSummary {

        private final int totalJobs;
        private final int inspectedJobs;
        private final int matchedJobs;
        private final int hiddenJobs;
        private final int openedJobs;
        private final int skippedJobs;

        public JobProcessingSummary(
                int totalJobs,
                int inspectedJobs,
                int matchedJobs,
                int hiddenJobs,
                int openedJobs,
                int skippedJobs) {

            this.totalJobs = totalJobs;
            this.inspectedJobs = inspectedJobs;
            this.matchedJobs = matchedJobs;
            this.hiddenJobs = hiddenJobs;
            this.openedJobs = openedJobs;
            this.skippedJobs = skippedJobs;
        }

        public int getTotalJobs() {
            return totalJobs;
        }

        public int getInspectedJobs() {
            return inspectedJobs;
        }

        public int getMatchedJobs() {
            return matchedJobs;
        }

        public int getHiddenJobs() {
            return hiddenJobs;
        }

        public int getOpenedJobs() {
            return openedJobs;
        }

        public int getSkippedJobs() {
            return skippedJobs;
        }
    }
}
