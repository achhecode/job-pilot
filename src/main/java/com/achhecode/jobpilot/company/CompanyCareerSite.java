package com.achhecode.jobpilot.company;

public enum CompanyCareerSite {
    // https://www.screener.in/market/IN08/IN0801/IN080101/
    NAUKRI("Naukri", "https://www.naukri.com/"),
    CAPGEMINI("Capgemini", "https://www.capgemini.com/in-en/careers/"),
    INFOSYS("Infosys", "https://career.infosys.com/joblist?countrycode=IN&companyhiringtype=IL"),
    HEXAWARE("Hexaware", "https://jobs.hexaware.com/#en/sites/CX_1"),
    PINNACLEU("Pinnacleu", "https://pinnacleu.hiresome.ai/"),
    AIPRISE("AI Prise", "https://www.aiprise.com/careers"),
    TECHMAHINDRA("Tech Mahindra", "https://www.techmahindra.com/careers/"),
    FUJITSU("Fujitsu", "https://www.jobs.global.fujitsu.com/"),
    VIRTUSA("Virtusa", "https://www.virtusa.com/careers"),
    IRIS_SOFTWARE("Iris Software", "https://www.irissoftware.com/careers/"),
    PAYTM("Paytm", "https://paytm.com/careers"),
    COGNIZANT("cognizant", "https://careers.cognizant.com/india-en/"),
    EPAM("Epam", "https://jobs.welcome.epam.in/jobs"),
    WIPRO("Wipro", "https://careers.wipro.com/"),
    WISSEN("Wissen", "https://careers.wissen.com/jobs/Careers"),
    HCLTECH("Hcl Technology", "https://careers.hcltech.com/"),
    OPTUM("Optum", "https://www.unitedhealthgroup.com/careers/in/work.html"),
    LUXOFT("Luxoft", "https://career.luxoft.com/jobs"),
    XORIANT("Xoriant", "https://careers.xoriant.com/in/en/search-results"),
    LTM("LTI Mindtree", "https://www.ltm.com/india-careers"),
    COFORGE("Co-Forge", "https://careers.coforge.com/coforge/"),
    MPHASIS("Mphasis", "https://careers.mphasis.com/home.html"),
    ASM("ASM", "https://www.asm.com/careers"),
    NEWGEN("Newgen", "https://omnirecruit.newgen.co.in/IShareReferral/CareerPortal.aspx"),
    HAPPIEST_MINDS("Happiest Minds", "https://careers.happiestminds.com/"),
    CSM("CSM", "https://www.csm.tech/explore-openings"),
    CGI("CGI", "https://www.cgi.com/india/en/india/careers"),
    CWAN("Clear Water", "https://cwan.com/company/careers/"),
    INNOCITO("Innocito", "https://innocito.com/careers"),
    INVENIO_GLOBAL("Invenio Global", "https://invenio-global.com/job-search/"),
    LOWES("Lowes", "https://talent.lowes.com/in/en/home"),
    COMVIVA("Comviva", "https://www.comviva.com/careers/explore-opportunity/"),
    ERICSSON("Ericsson", "https://www.ericsson.com/en/careers/global-locations/india"),
    NTT_DATA("NTT Data", "https://careers.nttdata.com/global/en/search-results"),
    GLOBAL_LOGIC("Global Logic", "https://www.globallogic.com/careers/"),
    WALMART("Walmart",
            "https://careers.walmart.com/us/en/home/careers-areas/technology/software-engineering-and-architecture"),
    Birlasoft("Birla Software", "https://jobs.birlasoft.com/"),
    GENPACT("Genpact", "https://www.genpact.com/careers"),
    ALTIMETRIK("Altimetrik", "https://www.altimetrik.com/careers/"),
    CELSIOR("CELSIOR", ""),
    // __("", ""),
    // __("", ""),
    // __("", ""),
    // __("", ""),
    ;

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
