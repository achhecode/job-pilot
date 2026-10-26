package com.achhecode.jobpilot.application.controller;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.application.dto.JobApplicationResponse;
import com.achhecode.jobpilot.application.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-applications")
public class JobApplicationController {
    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(@Valid @RequestBody JobApplicationRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public JobApplicationResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
