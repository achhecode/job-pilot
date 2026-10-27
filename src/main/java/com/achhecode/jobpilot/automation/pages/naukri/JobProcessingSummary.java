package com.achhecode.jobpilot.automation.pages.naukri;

import java.util.List;
import java.util.Map;

public record JobProcessingSummary(
        int inspected,
        List<String> shortlistedJobs,
        List<String> hiddenJobs,
        Map<String, ApplyResult> applyResults) {

    public JobProcessingSummary {
        shortlistedJobs = List.copyOf(shortlistedJobs);
        hiddenJobs = List.copyOf(hiddenJobs);
        applyResults = Map.copyOf(applyResults);
    }

    public long count(ApplyResult result) {
        return applyResults.values().stream().filter(r -> r == result).count();
    }
}
