package com.assessment.finance.model;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
@Entity @Table(name="financial_records")
public class FinancialRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, precision=15, scale=2) private BigDecimal amount;
 @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private RecordType type;
 @Column(nullable=false, length=100) private String category;
 @Column(nullable=false) private LocalDate date;
 @Column(length=500) private String notes;
 @Column(nullable=false, updatable=false) private Instant createdAt;
 @PrePersist void onCreate(){createdAt=Instant.now();}
 public Long getId(){return id;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
 public RecordType getType(){return type;} public void setType(RecordType v){type=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;}
 public LocalDate getDate(){return date;} public void setDate(LocalDate v){date=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
 public Instant getCreatedAt(){return createdAt;}
}
