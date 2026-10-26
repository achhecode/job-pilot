package com.achhecode.jobpilot.automation.service;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.exception.UnsupportedCompanyException;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.strategy.JobApplicationStrategy;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JobApplicationAutomationService {
    private final Map<String, JobApplicationStrategy> strategies;

    public JobApplicationAutomationService(List<JobApplicationStrategy> strategyList) {
        this.strategies = strategyList.stream().collect(Collectors.toUnmodifiableMap(
                strategy -> normalize(strategy.getCompanyId()),
                Function.identity()
        ));
    }

    public AutomationResult apply(
            JobAutomationContext context,
            JobApplicationRequest request,
            CandidateProfileResponse candidateProfileResponse
    ) {
        String jobId = normalize(request.company().getId());
        JobApplicationStrategy strategy = strategies.get(jobId);

        if (strategy == null) {
            throw new UnsupportedCompanyException(request.jobId());
        }

        return strategy.apply(context, request, candidateProfileResponse);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
