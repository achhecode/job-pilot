package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Filtering rules. A job is KEPT only when ALL of these hold:
 * 1. max experience >= minExperience
 * 2. salary undisclosed OR max salary > minSalaryLpa
 * 3. posted within maxDaysOld days
 * 4. at least one keyword appears in the job's tags (case-insensitive)
 *
 * @param maxJobsToInspect 0 = inspect every captured card
 */
public record JobFilter(
        int minExperience,
        int minSalaryLpa,
        int maxDaysOld,
        int maxJobsToInspect,
        List<String> keywords) {

    public JobFilter {
        if (minExperience < 0 || minSalaryLpa < 0
                || maxDaysOld < 0 || maxJobsToInspect < 0) {
            throw new IllegalArgumentException(
                    "Experience, salary, days and job limit cannot be negative.");
        }
        keywords = keywords == null ? List.of()
                : keywords.stream()
                        .map(k -> k.trim().toLowerCase(Locale.ROOT))
                        .filter(k -> !k.isEmpty())
                        .toList();
    }

    /**
     * Convenience factory: keywords as a comma separated string, e.g. "java,
     * spring".
     */
    public static JobFilter of(int minExperience, int minSalaryLpa, int maxDaysOld,
            int maxJobsToInspect, String commaSeparatedKeywords) {
        List<String> keywords = commaSeparatedKeywords == null
                ? List.of()
                : Arrays.asList(commaSeparatedKeywords.split(","));
        return new JobFilter(minExperience, minSalaryLpa, maxDaysOld,
                maxJobsToInspect, keywords);
    }

    public boolean matches(JobCard card) {
        return experienceMatches(card)
                && salaryMatches(card)
                && recencyMatches(card)
                && keywordsMatch(card);
    }

    /** Human readable reason, used for logging. */
    public String explain(JobCard card) {
        return "experience=%s, salary=%s, tags=%s, posted=%s".formatted(
                flag(experienceMatches(card)), flag(salaryMatches(card)),
                flag(keywordsMatch(card)), flag(recencyMatches(card)));
    }

    private boolean experienceMatches(JobCard card) {
        return card.maxExperience() >= minExperience;
    }

    private boolean salaryMatches(JobCard card) {
        return card.isSalaryUndisclosed() || card.maxSalaryLpa() > minSalaryLpa;
    }

    private boolean recencyMatches(JobCard card) {
        return card.daysSincePosted() <= maxDaysOld;
    }

    private boolean keywordsMatch(JobCard card) {
        if (keywords.isEmpty()) {
            return true;
        }
        String tags = String.join("|", card.tags()).toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(tags::contains);
    }

    private static String flag(boolean ok) {
        return ok ? "OK" : "FAIL";
    }
}
