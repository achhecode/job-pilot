package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * Workflow:
 * PHASE 1 -> inspect every card, HIDE the irrelevant ones, shortlist the rest.
 * PHASE 2 -> only when phase 1 fully succeeded, apply to the shortlist.
 */
@Slf4j
public class NaukriJobProcessor {

    private static final int PAUSE_BETWEEN_JOBS_MS = 1500;

    private final NaukriJobListingPage listingPage;
    private final Page browserPage;

    public NaukriJobProcessor(Page browserPage) {
        this.browserPage = browserPage;
        this.listingPage = new NaukriJobListingPage(browserPage);
    }

    public JobProcessingSummary process(JobFilter filter) {
        listingPage.configureTimeouts();

        FilterResult filtered = filterAndHide(filter); // throws -> phase 2 never runs
        Map<String, ApplyResult> results = applyToJobs(filtered.shortlisted());

        return new JobProcessingSummary(
                filtered.inspected(), filtered.shortlisted(), filtered.hidden(), results);
    }

    // ======================= PHASE 1: FILTER + HIDE =======================

    public record FilterResult(int inspected, List<String> shortlisted, List<String> hidden) {
    }

    public FilterResult filterAndHide(JobFilter filter) {
        log.info("========== PHASE 1: FILTER AND HIDE ==========");
        log.info("Listing URL: {}", listingPage.getCurrentUrl());

        listingPage.waitForJobs();
        List<String> jobIds = listingPage.getJobIds(); // captured before any hiding

        int limit = filter.maxJobsToInspect() == 0
                ? jobIds.size()
                : Math.min(filter.maxJobsToInspect(), jobIds.size());

        List<String> shortlisted = new ArrayList<>();
        List<String> hidden = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        log.info("Jobs found: {} | to inspect: {}", jobIds.size(), limit);

        for (int i = 0; i < limit; i++) {
            String jobId = jobIds.get(i);
            log.info("---- Job {}/{} | ID {} ----", i + 1, limit, jobId);

            try {
                JobCard card = listingPage.readJobCard(jobId);
                if (card == null) {
                    log.info("Job {} disappeared, skipping", jobId);
                    continue;
                }

                if (filter.matches(card)) {
                    shortlisted.add(jobId);
                    log.info("KEEP  {} | {} | {}", jobId, card.title(), filter.explain(card));
                } else if (listingPage.hideJob(jobId)) {
                    hidden.add(jobId);
                    log.info("HIDE  {} | {}", jobId, filter.explain(card));
                } else {
                    failed.add(jobId);
                }
            } catch (Exception e) {
                failed.add(jobId);
                log.error("Unexpected error while filtering job {}", jobId, e);
            }
        }

        log.info("========== FILTERING SUMMARY ==========");
        log.info("Inspected: {} | Kept: {} | Hidden: {} | Failed: {}",
                limit, shortlisted.size(), hidden.size(), failed.size());
        log.info("Shortlisted IDs: {}", shortlisted);

        if (!failed.isEmpty()) {
            throw new JobFilteringException(failed);
        }

        log.info("Filtering completed. Ready for the application phase.");
        return new FilterResult(limit, shortlisted, hidden);
    }

    // ======================== PHASE 2: APPLY ========================

    public Map<String, ApplyResult> applyToJobs(List<String> shortlisted) {
        log.info("========== PHASE 2: APPLY ({} jobs) ==========", shortlisted.size());

        Map<String, ApplyResult> results = new LinkedHashMap<>();

        for (int i = 0; i < shortlisted.size(); i++) {
            String jobId = shortlisted.get(i);
            log.info("---- Apply {}/{} | ID {} ----", i + 1, shortlisted.size(), jobId);

            results.put(jobId, applyToJob(jobId));
            browserPage.waitForTimeout(PAUSE_BETWEEN_JOBS_MS);
        }

        log.info("========== APPLICATION SUMMARY ==========");
        for (ApplyResult r : ApplyResult.values()) {
            long n = results.values().stream().filter(x -> x == r).count();
            if (n > 0) {
                log.info("{}: {}", r, n);
            }
        }
        return results;
    }

    private ApplyResult applyToJob(String jobId) {
        NaukriJobDetailsPage details = null;
        ApplyResult result = ApplyResult.ERROR;

        try {
            if (!listingPage.jobExists(jobId)) {
                log.warn("Shortlisted job {} no longer exists", jobId);
                return result = ApplyResult.JOB_UNAVAILABLE;
            }

            details = listingPage.openJobDetails(jobId);
            if (details == null) {
                return result = ApplyResult.JOB_UNAVAILABLE;
            }

            log.info("Opened: {}", details.getUrl());
            result = details.apply();
            log.info("Job {} -> {}", jobId, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to apply to job {}", jobId, e);
            return ApplyResult.ERROR;

        } finally {
            // Close only after the outcome is verified; keep tabs that need a human.
            if (details != null && !result.needsManualReview()) {
                details.close();
            } else if (details != null) {
                log.warn("Leaving tab open for manual review: {}", details.getUrl());
            }
        }
    }
}
