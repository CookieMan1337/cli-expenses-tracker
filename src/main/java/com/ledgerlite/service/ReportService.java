package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.io.CSVExporter;
import com.ledgerlite.io.JSONExporter;
import com.ledgerlite.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final LedgerService ledgerService;


    public ReportService(LedgerService ledgerService){
        this.ledgerService = ledgerService;
    }

    public Report getReportByPeriod(YearMonth period){
       Money income = ledgerService.getIncomesByPeriod(period).stream()
               .map(Income::getAmount)
               .reduce(new Money(BigDecimal.ZERO, Currency.getInstance("RUB")), Money::add);

       Money expense = ledgerService.getExpensesByPeriod(period).stream()
               .map(Expense::getAmount)
               .reduce(new Money(BigDecimal.ZERO, Currency.getInstance("RUB")), Money::add);

       Map<Category,Money> expensesInCategories = ledgerService.getExpensesInCategories(period);
       return  new Report(period, income, expense, expensesInCategories);
    }

    public List<Expense> getTopNExpenses(int n){
        return ledgerService.getAllTransactions().stream()
                .filter(t -> t instanceof Expense)
                .map(t-> (Expense) t)
                .sorted(Comparator.comparing(e -> e.getAmount().value(), Comparator.reverseOrder()))
                .limit(n)
                .toList();
    }

    public void exportReportByPeriod(YearMonth period,String type) throws IOException {
        Report reportByPeriod = getReportByPeriod(period);
        if (type.equalsIgnoreCase("json")){
            JSONExporter.export(reportByPeriod,"src/main/resources/reportByPeriod_"+period+".json");
            log.info("JSON export report by period finished");
        } else if (type.equalsIgnoreCase("csv")) {
            CSVExporter.export(reportByPeriod,"src/main/resources/reportByPeriod_"+period+".csv");
            log.info("CSV export report by period finished");

        }

    }

    public void exportTopNExpenses(int n,String type) throws Exception {
        List<Expense> topNExpenses = getTopNExpenses(n);
        if (type.equalsIgnoreCase("json")){
            JSONExporter.export(topNExpenses,"src/main/resources/topNExpenses.json");
            log.info("JSON export top expenses finished");
        } else if (type.equalsIgnoreCase("csv")) {
            CSVExporter.export(topNExpenses,"src/main/resources/topNExpenses.csv");
            log.info("CSV export top expenses finished");
        }

    }


}
