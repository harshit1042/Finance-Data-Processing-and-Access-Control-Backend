package com.assessment.finance.repository;

import com.assessment.finance.model.FinancialRecord;
import com.assessment.finance.model.RecordType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    List<FinancialRecord> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    Page<FinancialRecord> findByDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<FinancialRecord> findByTypeAndDateBetween(RecordType type, LocalDate from, LocalDate to, Pageable pageable);

    Page<FinancialRecord> findByCategoryIgnoreCaseAndDateBetween(
            String category, LocalDate from, LocalDate to, Pageable pageable);

    Page<FinancialRecord> findByTypeAndCategoryIgnoreCaseAndDateBetween(
            RecordType type, String category, LocalDate from, LocalDate to, Pageable pageable);

    List<FinancialRecord> findTop10ByOrderByDateDescCreatedAtDesc();
}
