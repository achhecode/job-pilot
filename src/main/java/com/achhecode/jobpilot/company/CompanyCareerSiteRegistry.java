package com.achhecode.jobpilot.company;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class CompanyCareerSiteRegistry {
    public List<CompanyCareerSite> findAll() {
        return List.copyOf(Arrays.asList(CompanyCareerSite.values()));
    }

    public Optional<CompanyCareerSite> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        try {
            return Optional.of(CompanyCareerSite.valueOf(id.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
