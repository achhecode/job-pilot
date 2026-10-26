package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.List;
import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

public class NaukriInboxPage {

    private static final String INBOX_URL = "https://www.naukri.com/mnjuser/inbox";

    private static final String APPLICATION_SUCCESS_URL = "https://www.naukri.com/myapply/saveApply";

    private static final String UNAPPLIED_JOBS_XPATH = "//div[@data-index and contains(@class, 'inbox-company-card')]"
            + "[not(.//div[@class='company-card']"
            + "/div[@class='tags-wrap']/span)]";

    private static final String APPLIED_JOBS_XPATH = "//div[@data-index and contains(@class, 'inbox-company-card')]"
            + "[.//div[@class='company-card']"
            + "/div[@class='tags-wrap']/span]";

    private static final Pattern JAVA_SKILL = Pattern.compile("(?i)^\\s*java\\s*$");

    private final Page page;

    public NaukriInboxPage(Page page) {
        this.page = page;
    }

    public void goToInbox() {
        page.navigate(INBOX_URL);
        waitForInbox();
    }

    private void waitForInbox() {
        page.locator(
                "//div[@data-index and contains(@class, 'inbox-company-card')]").first().waitFor(
                        new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(15_000));
    }

    /**
     * Checks whether a submission confirmation is visible.
     */
    public boolean isSubmissionConfirmed() {
        Locator confirmation = page.getByText(
                Pattern.compile(
                        "application submitted",
                        Pattern.CASE_INSENSITIVE));

        return confirmation.count() > 0
                && confirmation.first().isVisible();
    }

    /**
     * Returns the number of jobs currently displayed as unapplied.
     */
    public int getUnappliedJobCount() {
        return page.locator(UNAPPLIED_JOBS_XPATH).count();
    }

    /**
     * Returns the number of jobs currently displayed as applied.
     */
    public int getAppliedJobCount() {
        return page.locator(APPLIED_JOBS_XPATH).count();
    }

    /**
     * Processes at most maxJobs unapplied job cards.
     *
     * maxJobs counts jobs opened for processing, not just
     * successful applications.
     *
     * The caller must already be logged in.
     */
    public void processUnappliedJobs(int maxJobs) {
        if (maxJobs < 0) {
            throw new IllegalArgumentException(
                    "maxJobs cannot be negative");
        }

        int processed = 0;

        goToInbox();

        while (processed < maxJobs) {

            Locator unappliedJobs = page.locator(UNAPPLIED_JOBS_XPATH);

            if (unappliedJobs.count() == 0) {
                System.out.println(
                        "No unapplied jobs remain.");
                break;
            }

            // Always select the first remaining unapplied job.
            Locator job = unappliedJobs.first();

            job.scrollIntoViewIfNeeded();
            job.click();

            processed++;

            System.out.printf(
                    "Processing job %d of %d%n",
                    processed,
                    maxJobs);

            try {
                processSelectedJob();
            } catch (Exception exception) {
                System.err.println(
                        "Could not finish job "
                                + processed
                                + ": "
                                + exception.getMessage());
            }

            // Revisit the inbox and re-evaluate the current DOM.
            // Do not reuse old card locators after navigation.
            goToInbox();

            // A short delay between jobs.
            page.waitForTimeout(2_000);
        }

        System.out.printf(
                "Finished. Processed %d of at most %d jobs.%n",
                processed,
                maxJobs);
    }

    /**
     * Processes the currently selected job detail.
     */
    private void processSelectedJob() {

        Locator skillsLocator = page.locator("span.job-keyskills");

        skillsLocator.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(10_000));

        String skillsText = skillsLocator.innerText();

        if (!containsJavaSkill(skillsText)) {
            System.out.println(
                    "Java skill not found. Marking not interested.");

            markNotInterested();
            return;
        }

        System.out.println(
                "Java skill found. Applying to job.");

        applyToJob();
    }

    /**
     * Matches Java as a comma-separated skill, ignoring case.
     *
     * Matches:
     * Java, java, JAVA
     *
     * Does not match:
     * JavaScript, Core JavaScript
     */
    private boolean containsJavaSkill(String skillsText) {

        if (skillsText == null || skillsText.isBlank()) {
            return false;
        }

        return List.of(skillsText.split(","))
                .stream()
                .map(String::trim)
                .anyMatch(skill -> JAVA_SKILL.matcher(skill).matches());
    }

    /**
     * Marks a job as not interested using the requested reasons.
     */
    private void markNotInterested() {

        page.locator("button.not-interested").click();

        Locator modal = page.locator("div.not-interested-modal");

        modal.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));

        // Select both reasons.
        selectReason(modal, "Job role");
        selectReason(modal, "Key skills");

        Locator confirmButton = modal.locator("button.blue-btn");

        confirmButton.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));

        confirmButton.click();

        // Allow the inbox to update.
        page.waitForTimeout(2_000);
    }

    private void selectReason(Locator modal, String reason) {

        Locator pill = modal.locator(".options-cnt .pill")
                .filter(
                        new Locator.FilterOptions()
                                .setHasText(
                                        Pattern.compile(
                                                "^\\s*"
                                                        + Pattern.quote(reason)
                                                        + "\\s*$")));

        if (pill.count() == 0) {
            throw new IllegalStateException(
                    "Not Interested reason not found: " + reason);
        }

        pill.first().click();
    }

    /**
     * Clicks Apply and detects either a popup tab or a chatbot drawer.
     *
     * Chatbot question answering is intentionally left for a
     * separate handler.
     */
    private void applyToJob() {

        Locator applyButton = page.locator("button.apply-btn");

        applyButton.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));

        Page popup = null;

        try {
            popup = page.waitForPopup(
                    new Page.WaitForPopupOptions()
                            .setTimeout(3_000),
                    () -> applyButton.click());
        } catch (TimeoutError exception) {
            // No popup appeared within the timeout.
            // The chatbot may have opened on the current page.
        }

        if (popup != null) {
            System.out.println(
                    "Apply opened a new tab: " + popup.url());

            if (popup.getByText(
                    Pattern.compile(
                            "application submitted|successfully applied",
                            Pattern.CASE_INSENSITIVE))
                    .count() > 0) {
                System.out.println(
                        "Application confirmation detected in popup.");
            } else {
                System.out.println(
                        "Popup opened; verify application status.");
            }

            // Do not close the popup automatically.
            // It may contain important application information.
            return;
        }

        Locator chatbotDrawer = page.locator("div.chatbot_Drawer.chatbot_right");

        if (chatbotDrawer.count() > 0
                && chatbotDrawer.first().isVisible()) {

            System.out.println(
                    "Chatbot drawer opened. "
                            + "Question handling is pending.");

            // TODO:
            // 1. Read the latest bot question.
            // 2. Resolve the answer using candidate profile data.
            // 3. Handle text inputs, options and checkboxes.
            // 4. Send the answer and wait for the next question.
            // 5. Stop when the application is confirmed.
            return;
        }

        if (isSubmissionConfirmed()) {
            System.out.println(
                    "Application submission confirmation detected.");
            return;
        }

        System.out.println(
                "Apply was clicked, but application status "
                        + "could not be confirmed.");
    }

    public boolean isApplicationSuccessfullySubmitted() {
        return page.url().contains(APPLICATION_SUCCESS_URL);
    }

    public boolean waitForApplicationSubmission() {
        try {
            page.waitForURL(
                    url -> url.contains("/myapply/saveApply"),
                    new Page.WaitForURLOptions()
                            .setTimeout(10_000));

            return true;
        } catch (TimeoutError e) {
            return isApplicationSuccessfullySubmitted();
        }
    }
}
