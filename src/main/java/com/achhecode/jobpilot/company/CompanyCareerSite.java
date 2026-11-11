package com.achhecode.jobpilot.company;

public enum CompanyCareerSite {
    // https://www.screener.in/market/IN08/IN0801/IN080101/
    NAUKRI("Naukri", "https://www.naukri.com/"),
    CAPGEMINI("Capgemini", "https://www.capgemini.com/in-en/careers/"),
    INFOSYS("Infosys", "https://career.infosys.com/joblist"),
    HEXAWARE("Hexaware", "https://jobs.hexaware.com/#en/sites/CX_1"),
    PINNACLEU("pinnacleu", "https://pinnacleu.hiresome.ai/"),
    AIPRISE("aiprise", "https://www.aiprise.com/careers"),
    TECHMAHINDRA("techmahindra", "https://www.techmahindra.com/careers/"),
    FUJITSU("fujitsu", "https://www.jobs.global.fujitsu.com/"),
    VIRTUSA("virtusa", "https://www.virtusa.com/careers"),
    IRIS_SOFTWARE("irissoftware", "https://www.irissoftware.com/careers/"),
    PAYTM("paytm", "https://paytm.com/careers"),
    COGNIZANT("cognizant", "https://careers.cognizant.com/india-en/"),
    EPAM("epam", "https://jobs.welcome.epam.in/jobs"),
    WIPRO("wipro", "https://careers.wipro.com/"),
    WISSEN("wissen", "https://careers.wissen.com/jobs/Careers"),
    HCLTECH("hcltech", "https://careers.hcltech.com/"),
    OPTUM("optum", "https://www.unitedhealthgroup.com/careers/in/work.html"),
    LUXOFT("luxoft", "https://career.luxoft.com/jobs"),
    XORIANT("xoriant", "https://careers.xoriant.com/in/en/search-results"),
    LTM("ltm", "https://www.ltm.com/india-careers"),
    COFORGE("Co-Forge", "https://careers.coforge.com/coforge/"),
    MPHASIS("mphasis", "https://careers.mphasis.com/home.html"),
    ASM("asm", "https://www.asm.com/careers"),
    NEWGEN("Newgen", "https://omnirecruit.newgen.co.in/IShareReferral/CareerPortal.aspx"),
    HAPPIEST_MINDS("Happiest Minds", "https://careers.happiestminds.com/"),
    CSM("csm", "https://www.csm.tech/explore-openings"),
    CGI("cgi", "https://www.cgi.com/india/en/india/careers");

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
