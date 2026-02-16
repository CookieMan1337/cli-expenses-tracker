package report;

import domain.Expense;
import service.LedgerService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportByPeriod {

    public Map<String, Object> getReportByPeriod(LocalDate startDate, LocalDate endDate, LedgerService ledgerService) {
        List<Expense> monthlyExpenses = ledgerService.getExpRep().findAll().stream()
                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                .sorted(Comparator.comparing(Expense::getDate))
                .collect(Collectors.toList());

        if (monthlyExpenses.isEmpty()) {
            Map<String, Object> emptyReport = new HashMap<>();
            emptyReport.put("month", startDate + "_" + endDate);
            emptyReport.put("message", "No expenses found");
            return emptyReport;
        }

        Map<String, Object> report = new HashMap<>();

        //Всего расходов
        report.put("month", startDate + "_" + endDate);
        report.put("totalExpenses", monthlyExpenses.size());
        report.put("totalAmount", monthlyExpenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum());

        //сумма трат по категориям
        Map<String, Double> byCategory = monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryCode,
                        Collectors.summingDouble(Expense::getAmount)
                ));
        report.put("byCategory", byCategory);

        //сумма всех расходов по дням
        Map<LocalDate, Double> byDay = monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getDate,
                        Collectors.summingDouble(Expense::getAmount)
                ));
        report.put("byDay", byDay);

        //полноая детализация расходов
        report.put("details", monthlyExpenses);

        return report;

    }

}
