package com.achhecode.jobpilot.automation.pages.infosys;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Infosys career-site search page. Selectors are placeholders until verified.
 */
public class InfosysJobSearchPage {
    private final Page page;

    public InfosysJobSearchPage(Page page) {
        this.page = page;
    }

    public void open(String careerUrl) {
        page.navigate(careerUrl);
    }

    public void searchJobs(String keywords, String location) {
        // TODO Replace with current Infosys selectors.
        fillIfPresent("input[name='keyword']", keywords);
        if (location != null && !location.isBlank()) {
            fillIfPresent("input[name='location']", location);
        }

        Locator searchButton = page.locator("button[type='submit']");
        if (searchButton.count() > 0) searchButton.first().click();
        else throw new IllegalStateException("Configure the Infosys job-search button selector.");
    }

    public String selectJob(String jobUrl, String jobTitle) {
        if (jobUrl != null && !jobUrl.isBlank()) {
            page.navigate(jobUrl);
            return page.url();
        }

        // TODO Implement listing selection by title after inspecting the current page DOM.
        throw new UnsupportedOperationException(
                "Infosys result matching by title has not been implemented.");
    }

    public void clickApply() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Apply").setExact(false)).click();
    }

    private void fillIfPresent(String selector, String value) {
        if (value == null || value.isBlank()) return;
        Locator locator = page.locator(selector).first();
        if (locator.count() > 0) locator.fill(value);
        else throw new IllegalStateException("Required Infosys selector not found: " + selector);
    }
}
