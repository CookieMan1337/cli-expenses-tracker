package com.ledgerlite.app;

import com.ledgerlite.io.importer.CsvImporter;
import com.ledgerlite.persistence.InMemoryBudgetRepository;
import com.ledgerlite.persistence.InMemoryCategoryRepository;
import com.ledgerlite.persistence.InMemoryTransactionRepository;
import com.ledgerlite.service.BudgetService;
import com.ledgerlite.service.LedgerService;
import com.ledgerlite.service.ReportService;
import com.ledgerlite.service.ServiceContainer;
import com.ledgerlite.util.BudgetMapper;
import com.ledgerlite.util.TransactionMapper;

public class AppConfig {

    public static ServiceContainer createServiceContainer() {
        InMemoryCategoryRepository categoryStorage = new InMemoryCategoryRepository();
        InMemoryTransactionRepository transactionStorage = new InMemoryTransactionRepository();
        InMemoryBudgetRepository budgetStorage = new InMemoryBudgetRepository();

        TransactionMapper transactionMapper = new TransactionMapper(categoryStorage, "RUB");
        BudgetMapper budgetMapper = new BudgetMapper(categoryStorage, "RUB");

        LedgerService ledgerService = new LedgerService(transactionStorage, categoryStorage);
        BudgetService budgetService = new BudgetService(budgetStorage);
        ReportService reportService = new ReportService(transactionStorage);

        CsvImporter csvImporter = new CsvImporter(ledgerService, transactionMapper);

        return new ServiceContainer(
                ledgerService,
                budgetService,
                reportService,
                transactionMapper,
                budgetMapper,
                csvImporter
        );
    }
}
