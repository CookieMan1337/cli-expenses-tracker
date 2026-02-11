package com.ledgerlite.io;

import com.ledgerlite.domain.*;
import com.ledgerlite.io.JSONExporter;
import com.ledgerlite.persistence.FileStore;
import com.ledgerlite.persistence.InMemoryRepository;
import com.ledgerlite.persistence.Repository;
import com.ledgerlite.report.Report;
import com.ledgerlite.service.LedgerService;
import com.ledgerlite.service.ReportService;
import com.ledgerlite.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JSONEsporterTest {
    private final Repository<Transaction> transactionRepository = new InMemoryRepository<>();
    private final Map<YearMonth, Map<Category, Budget>> budgets = new ConcurrentHashMap<>();
    private LedgerService ledgerService;
    private ReportService reportService;

    private final FileStore fileStore = new FileStore();

    private final Currency RUB = Currency.getInstance("RUB");
    private Category food;
    private Category entertainment;

    @BeforeEach
    void setUp(){
        ledgerService = new LedgerService(transactionRepository,budgets,fileStore);
        reportService = new ReportService(ledgerService);

        food = new Category("FOOD", "Food");
        ledgerService.addCategory(food.code(),food.name());

        entertainment = new Category("ENTERT", "Entertainment");
        ledgerService.addCategory(entertainment.code(),entertainment.name());

        ledgerService.addExpense(LocalDate.of(2026,1,20),new Money(BigDecimal.valueOf(1000), RUB),food, "еда");
        ledgerService.addExpense(LocalDate.of(2026,1,21),new Money(BigDecimal.valueOf(3000), RUB),entertainment, "развлечения");

    }

    @Test
    void testExportList() throws Exception {
        List<Expense> topNExpenses = reportService.getTopNExpenses(2);
        JSONExporter.export(topNExpenses,"src/test/resources/topNExpenses.json");
    }

    @Test
    void testExport() throws Exception {
        YearMonth period = YearMonth.from(DateUtil.parsePeriod("2026-01"));
        Report reportByPeriod = reportService.getReportByPeriod(period);
        JSONExporter.export(reportByPeriod,"src/test/resources/reportByPeriod_"+period+".json");
    }

}
