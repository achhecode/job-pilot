package com.achhecode.jobpilot.company;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CareerSiteController {

    @SuppressWarnings("null")
    @GetMapping("/career-sites")
    public String careerSites(Model model) {

        List<CompanyCareerSite> companies = Arrays.stream(
                CompanyCareerSite.values())
                .sorted(Comparator.comparing(
                        CompanyCareerSite::getTitle,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("companies", companies);
        model.addAttribute("totalCompanies", companies.size());

        return "career-sites";
    }
}
