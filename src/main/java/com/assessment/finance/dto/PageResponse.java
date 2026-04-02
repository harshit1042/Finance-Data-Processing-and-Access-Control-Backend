package com.assessment.finance.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int pageSize,
        int pageNumber) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                // Convert Spring's 0-based page index to 1-based for client friendliness.
                page.getNumber() + 1);
    }
}
