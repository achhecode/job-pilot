package com.achhecode.jobpilot.automation.service;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.exception.UnsupportedCompanyException;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.strategy.JobApplicationStrategy;
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
            JobApplicationRequest request
    ) {
        String companyId = normalize(request.companyId());
        JobApplicationStrategy strategy = strategies.get(companyId);

        if (strategy == null) {
            throw new UnsupportedCompanyException(request.companyId());
        }

        return strategy.apply(context, request);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
