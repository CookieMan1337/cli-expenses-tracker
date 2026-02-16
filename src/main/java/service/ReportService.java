package service;

import domain.Expense;
import report.ReportByPeriod;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {

    private final LedgerService ledgerService;

    ReportByPeriod reportByPeriod = new ReportByPeriod();

    public ReportService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    //модный отчет трат за месяц
    public Map<String, Object> getReportByMonth(String yearMonth) {
        try {
            YearMonth targetMonth = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));

            LocalDate startOfMonth = targetMonth.atDay(1);
            LocalDate endOfMonth = targetMonth.atEndOfMonth();

            return reportByPeriod.getReportByPeriod(startOfMonth, endOfMonth, ledgerService);

        } catch (DateTimeParseException e) {
            Map<String, Object> errorReport = new HashMap<>();
            errorReport.put("error", "Uncorrected date, expecting format: YYYY-MM");
            return errorReport;
        }

    }

    //отчет топ-N расходов
    public List<Expense> getTopReport(String numberOfTop) {
        try {
            int number = Integer.parseInt(numberOfTop);
            return ledgerService.getExpRep().findAll().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()))
                    .limit(number)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            System.out.println("Uncorrected number");
            return null;
        }
    }
}
