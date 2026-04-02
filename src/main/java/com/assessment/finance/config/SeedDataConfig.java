package com.assessment.finance.config;

import com.assessment.finance.model.FinancialRecord;
import com.assessment.finance.model.RecordType;
import com.assessment.finance.model.Role;
import com.assessment.finance.model.User;
import com.assessment.finance.model.UserStatus;
import com.assessment.finance.repository.FinancialRecordRepository;
import com.assessment.finance.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedDataConfig {

    /** Plain password for built-in demo accounts (README documents this). */
    private static final String DEMO_PASSWORD = "password";

    @Bean
    CommandLineRunner seed(
            UserRepository userRepo, FinancialRecordRepository recRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            syncBuiltInUsers(userRepo, passwordEncoder);

            if (recRepo.count() == 0) {
                FinancialRecord r1 = new FinancialRecord();
                r1.setAmount(new BigDecimal("5000.00"));
                r1.setType(RecordType.INCOME);
                r1.setCategory("Salary");
                r1.setDate(LocalDate.now().minusDays(20));
                r1.setNotes("Monthly salary");
                recRepo.save(r1);

                FinancialRecord r2 = new FinancialRecord();
                r2.setAmount(new BigDecimal("1200.00"));
                r2.setType(RecordType.EXPENSE);
                r2.setCategory("Rent");
                r2.setDate(LocalDate.now().minusDays(18));
                r2.setNotes("Apartment rent");
                recRepo.save(r2);
            }
        };
    }

    /**
     * Ensures admin / analyst / viewer exist and always use a known BCrypt hash for {@value #DEMO_PASSWORD}.
     * Fixes 401s when the DB was created before passwords existed or hashes were wrong/outdated.
     */
    private void syncBuiltInUsers(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        List<Builtin> builtins =
                List.of(
                        new Builtin("admin", Role.ADMIN),
                        new Builtin("analyst", Role.ANALYST),
                        new Builtin("viewer", Role.VIEWER));
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        for (Builtin b : builtins) {
            User u = userRepo.findByUsername(b.username).orElseGet(User::new);
            u.setUsername(b.username);
            u.setPasswordHash(hash);
            u.setRole(b.role);
            u.setStatus(UserStatus.ACTIVE);
            userRepo.save(u);
        }
    }

    private record Builtin(String username, Role role) {}
}
