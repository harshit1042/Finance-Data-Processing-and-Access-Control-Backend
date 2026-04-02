package com.assessment.finance.service;
import com.assessment.finance.dto.SummaryResponse; import com.assessment.finance.model.*; import com.assessment.finance.repository.FinancialRecordRepository; import java.math.BigDecimal; import java.time.*; import java.util.*; import org.springframework.stereotype.Service;
@Service
public class SummaryService {
 private final FinancialRecordRepository repo; private final RecordService recordService; public SummaryService(FinancialRecordRepository repo, RecordService recordService){this.repo=repo; this.recordService=recordService;}
 public SummaryResponse getDashboardSummary(int monthsBack){ if(monthsBack<1||monthsBack>24) monthsBack=6; LocalDate start=YearMonth.now().minusMonths(monthsBack-1L).atDay(1); List<FinancialRecord> records=repo.findByDateBetweenOrderByDateDesc(start, LocalDate.now()); BigDecimal income=BigDecimal.ZERO, expense=BigDecimal.ZERO; Map<String,BigDecimal> cat=new HashMap<>(); Map<YearMonth,BigDecimal> mi=new HashMap<>(), me=new HashMap<>();
  for(FinancialRecord r:records){ if(r.getType()==RecordType.INCOME){ income=income.add(r.getAmount()); mi.merge(YearMonth.from(r.getDate()),r.getAmount(),BigDecimal::add);} else { expense=expense.add(r.getAmount()); me.merge(YearMonth.from(r.getDate()),r.getAmount(),BigDecimal::add);} cat.merge(r.getCategory(),r.getAmount(),BigDecimal::add);} 
  List<SummaryResponse.CategoryTotal> categoryTotals=cat.entrySet().stream().map(e->new SummaryResponse.CategoryTotal(e.getKey(),e.getValue())).sorted(Comparator.comparing(SummaryResponse.CategoryTotal::amount).reversed()).toList();
  List<SummaryResponse.TrendPoint> trends=new ArrayList<>(); for(int i=monthsBack-1;i>=0;i--){ YearMonth ym=YearMonth.now().minusMonths(i); BigDecimal inm=mi.getOrDefault(ym,BigDecimal.ZERO); BigDecimal exm=me.getOrDefault(ym,BigDecimal.ZERO); trends.add(new SummaryResponse.TrendPoint(ym.toString(),inm,exm,inm.subtract(exm))); }
  return new SummaryResponse(income,expense,income.subtract(expense),categoryTotals,recordService.recent(),trends);
 }
}
