package com.achhecode.jobpilot.company;

public enum CompanyCareerSite {
    NAUKRI("Naukri", "https://www.naukri.com/"),
    CAPGEMINI("Capgemini", "https://www.capgemini.com/in-en/careers/"),
    INFOSYS("Infosys", "https://career.infosys.com/joblist");

    private final String title;
    private final String url;

    CompanyCareerSite(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getId() {
        return name();
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
