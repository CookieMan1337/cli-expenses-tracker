package com.ledgerlite.report;


import com.ledgerlite.domain.Money;
import com.ledgerlite.domain.Transaction;

import java.math.RoundingMode;


import java.util.List;

public class ReportFormatter {
    public String format(PeriodSummary summary) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("📊 ОТЧЁТ: %s - %s%n",
                summary.from(), summary.to()));
        sb.append(String.format("   Доходы:  %s%n", summary.totalIncome()));
        sb.append(String.format("   Расходы: %s%n", summary.totalExpense()));
        sb.append(String.format("   Баланс:  %s%n", summary.balance()));

        if (!summary.topCategories().isEmpty()) {
            sb.append(String.format("%n   РАСХОДЫ ПО КАТЕГОРИЯМ:%n"));

            for (CategoryExpense ce : summary.topCategories()) {
                double percent = calculatePercent(ce.amount(), summary.totalExpense());
                sb.append(String.format("   %s: %s (%.1f%%)%n",
                        ce.category().name(), ce.amount(), percent));
            }
        }

        return sb.toString();
    }

    public String formatTopExpenses(List<Transaction> topExpenses) {
        // форматирование топа расходов
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 ТОП-10 РАСХОДОВ\n");
        // ... логика форматирования
        return sb.toString();
    }

    private double calculatePercent(Money part, Money total) {
        if (total.value().doubleValue() == 0) return 0;
        return part.value().divide(total.value(), 4, RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }

}
