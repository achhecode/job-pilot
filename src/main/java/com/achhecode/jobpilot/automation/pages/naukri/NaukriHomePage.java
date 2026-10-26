package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.regex.Pattern;

import com.microsoft.playwright.Page;

public class NaukriHomePage {

    private final Page page;

    public NaukriHomePage(Page page) {
        this.page = page;
    }

    public boolean isSubmissionConfirmed() {
        // TODO Replace with a reliable site-specific confirmation message/reference.
        return page.getByText(
                Pattern.compile("application submitted", Pattern.CASE_INSENSITIVE)).count() > 0;
    }



}
