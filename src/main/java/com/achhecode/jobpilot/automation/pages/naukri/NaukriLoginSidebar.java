package com.achhecode.jobpilot.automation.pages.naukri;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class NaukriLoginSidebar {

    private final Page page;

    // Locators
    private final Locator loginLink;
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;

    public NaukriLoginSidebar(Page page) {
        this.page = page;

        this.loginLink = page.locator("//a[@id='login_Layer']");

        this.usernameInput = page.locator(
                "//input[@placeholder='Enter your active Email ID / Username']");

        this.passwordInput = page.locator(
                "//input[@placeholder='Enter your password']");

        this.loginButton = page.locator(
                "//button[normalize-space()='Login']");
    }

    /**
     * Opens the login dialog.
     */
    public void clickLogin() {
        loginLink.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        loginLink.click();
    }

    /**
     * Enters the user's email or username.
     */
    public void enterUsername(String username) {
        usernameInput.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        usernameInput.fill(username);
    }

    /**
     * Enters the user's password.
     */
    public void enterPassword(String password) {
        passwordInput.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        passwordInput.fill(password);
    }

    /**
     * Submits the login form.
     */
    public void login() {
        loginButton.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        loginButton.click();
    }

    /**
     * Completes the login form using the supplied credentials.
     * Assumes the login dialog is already open.
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        login();
    }

    /**
     * Opens the login dialog and submits credentials.
     */
    public void clickLoginAndAuthenticate(
            String username,
            String password) {
        clickLogin();
        login(username, password);
    }

}
