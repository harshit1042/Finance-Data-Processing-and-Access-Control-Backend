package com.assessment.finance.service;

import com.assessment.finance.dto.*;
import com.assessment.finance.exception.ApiException;
import com.assessment.finance.model.*;
import com.assessment.finance.repository.FinancialRecordRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RecordService {

    private final FinancialRecordRepository repo;

    public RecordService(FinancialRecordRepository repo) {
        this.repo = repo;
    }

    public RecordResponse create(CreateRecordRequest req) {
        FinancialRecord r = new FinancialRecord();
        apply(r, req.amount(), req.type(), req.category(), req.date(), req.notes());
        return toResponse(repo.save(r));
    }

    public PageResponse<RecordResponse> list(
            RecordType type, String category, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (fromDate == null) {
            fromDate = LocalDate.of(1970, 1, 1);
        }
        if (toDate == null) {
            toDate = LocalDate.now().plusYears(10);
        }
        if (fromDate.isAfter(toDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate");
        }
        Page<FinancialRecord> page;
        if (type != null && category != null) {
            page = repo.findByTypeAndCategoryIgnoreCaseAndDateBetween(type, category, fromDate, toDate, pageable);
        } else if (type != null) {
            page = repo.findByTypeAndDateBetween(type, fromDate, toDate, pageable);
        } else if (category != null) {
            page = repo.findByCategoryIgnoreCaseAndDateBetween(category, fromDate, toDate, pageable);
        } else {
            page = repo.findByDateBetween(fromDate, toDate, pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    public RecordResponse update(Long id, UpdateRecordRequest req) {
        FinancialRecord r = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Record not found"));
        apply(r, req.amount(), req.type(), req.category(), req.date(), req.notes());
        return toResponse(repo.save(r));
    }

    public void delete(Long id) {
        FinancialRecord r = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Record not found"));
        repo.delete(r);
    }

    public List<RecordResponse> recent() {
        return repo.findTop10ByOrderByDateDescCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    private void apply(
            FinancialRecord r,
            java.math.BigDecimal amount,
            RecordType type,
            String category,
            LocalDate date,
            String notes) {
        r.setAmount(amount);
        r.setType(type);
        r.setCategory(category.trim());
        r.setDate(date);
        r.setNotes(notes == null ? null : notes.trim());
    }

    private RecordResponse toResponse(FinancialRecord r) {
        return new RecordResponse(
                r.getId(), r.getAmount(), r.getType(), r.getCategory(), r.getDate(), r.getNotes(), r.getCreatedAt());
    }
}
