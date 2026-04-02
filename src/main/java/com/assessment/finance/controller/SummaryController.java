package com.assessment.finance.controller;

import com.assessment.finance.dto.SummaryResponse;
import com.assessment.finance.model.Role;
import com.assessment.finance.security.RoleAccess;
import com.assessment.finance.service.SummaryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RoleAccess({Role.ADMIN, Role.ANALYST, Role.VIEWER})
@SecurityRequirement(name = "bearer")
@Tag(name = "Dashboard")
public class SummaryController {

    private final SummaryService service;

    public SummaryController(SummaryService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@RequestParam(defaultValue = "6") int monthsBack) {
        return service.getDashboardSummary(monthsBack);
    }
}
