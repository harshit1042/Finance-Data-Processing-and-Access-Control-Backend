package com.assessment.finance.controller;

import com.assessment.finance.dto.*;
import com.assessment.finance.model.RecordType;
import com.assessment.finance.model.Role;
import com.assessment.finance.security.RoleAccess;
import com.assessment.finance.service.RecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@Validated
@SecurityRequirement(name = "bearer")
@Tag(name = "Records")
public class RecordController {

    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleAccess(Role.ADMIN)
    public RecordResponse create(@Valid @RequestBody CreateRecordRequest req) {
        return service.create(req);
    }

    @GetMapping
    @RoleAccess({Role.ADMIN, Role.ANALYST})
    public PageResponse<RecordResponse> list(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("createdAt")));
        return service.list(type, category, fromDate, toDate, pageable);
    }

    @PutMapping("/{id}")
    @RoleAccess(Role.ADMIN)
    public RecordResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRecordRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RoleAccess(Role.ADMIN)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
