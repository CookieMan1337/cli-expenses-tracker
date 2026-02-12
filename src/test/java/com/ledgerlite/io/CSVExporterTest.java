package com.ledgerlite.io;

import com.ledgerlite.domain.*;
import com.ledgerlite.io.CSVExporter;
import com.ledgerlite.persistence.FileStore;
import com.ledgerlite.persistence.InMemoryRepository;
import com.ledgerlite.persistence.Repository;
import com.ledgerlite.report.Report;
import com.ledgerlite.service.LedgerService;
import com.ledgerlite.service.ReportService;
import com.ledgerlite.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CSVExporterTest {
    private final Repository<Transaction> transactionRepository = new InMemoryRepository<>();
    private final Map<YearMonth, Map<Category, Budget>> budgets = new ConcurrentHashMap<>();
    private LedgerService ledgerService;
    private ReportService reportService;
    //private final CSVExporter csvExporter = new CSVExporter();
    private FileStore fileStore;
    private Path projectDataDir;

    private final Currency RUB = Currency.getInstance("RUB");
    private Category food = new Category("FOOD", "Food");
    private Category entertainment;

    @BeforeEach
    void setUp(){
        fileStore = new FileStore();
        projectDataDir = Paths.get("").toAbsolutePath().resolve("data");
        cleanupTestData();

        ledgerService = new LedgerService(transactionRepository,budgets,fileStore);
        reportService = new ReportService(ledgerService);

        food = new Category("FOOD", "Food");
        ledgerService.addCategory(food.code(),food.name());

        entertainment = new Category("ENTERT", "Entertainment");
        ledgerService.addCategory(entertainment.code(),entertainment.name());

        ledgerService.addExpense(LocalDate.of(2026,1,20),new Money(BigDecimal.valueOf(1000), RUB),food, "еда");
        ledgerService.addExpense(LocalDate.of(2026,1,21),new Money(BigDecimal.valueOf(3000), RUB),entertainment, "развлечения");

    }

    private void cleanupTestData() {
        try {
            Path transactionsFile = projectDataDir.resolve("transactions.dat");
            if (Files.exists(transactionsFile)) {
                Path backup = projectDataDir.resolve("transactions.backup." +
                        System.currentTimeMillis() + ".dat");
                Files.move(transactionsFile, backup, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Создан backup: " + backup.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Не удалось очистить тестовые данные: " + e.getMessage());
        }
        try {
            Path catsFile = projectDataDir.resolve("categories.dat");
            if (Files.exists(catsFile)) {
                Path backup = projectDataDir.resolve("categories.backup." +
                        System.currentTimeMillis() + ".dat");
                Files.move(catsFile, backup, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Создан backup: " + backup.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Не удалось очистить тестовые данные: " + e.getMessage());
        }
    }

    @Test
    void testExportList() throws Exception {
        List<Expense> topNExpenses = reportService.getTopNExpenses(2);
        CSVExporter.export(topNExpenses,"src/test/resources/topNExpenses.csv");
    }

    @Test
    void testExport() throws Exception {
        YearMonth period = YearMonth.from(DateUtil.parsePeriod("2026-01"));
        Report reportByPeriod = reportService.getReportByPeriod(period);
        CSVExporter.export(reportByPeriod,"src/test/resources/reportByPeriod_"+period+".csv");
    }



}
