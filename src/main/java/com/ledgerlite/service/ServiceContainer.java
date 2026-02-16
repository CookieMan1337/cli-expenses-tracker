package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.ValidationException;

import com.ledgerlite.io.exporter.ReportExporter;
import com.ledgerlite.report.ExportReportItem;
import com.ledgerlite.io.importer.CsvImporter;
import com.ledgerlite.io.importer.ImportResult;

import com.ledgerlite.util.BudgetMapper;
import com.ledgerlite.util.ExporterFactory;
import com.ledgerlite.util.TransactionMapper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServiceContainer {
    private final LedgerService ledgerService;
    private final BudgetService budgetService;
    private final ReportService reportService;

    private final TransactionMapper transactionMapper;
    private final BudgetMapper budgetMapper;
    private final CsvImporter csvImporter;

    private final Deque<Runnable> undoStack = new ArrayDeque<>(10);

    public ServiceContainer(LedgerService ledgerService,
                            BudgetService budgetService,
                            ReportService reportService,
                            TransactionMapper transactionMapper,
                            BudgetMapper budgetMapper,
                            CsvImporter csvImporter
    ) {
        this.ledgerService = ledgerService;
        this.budgetService = budgetService;
        this.reportService = reportService;
        this.transactionMapper = transactionMapper;
        this.budgetMapper = budgetMapper;
        this.csvImporter = csvImporter;
    }

    public Category addCategory(String code, String name) {
        return ledgerService.addCategory(code, name);
    }

    public List<Category> getAllCategories() {
        return ledgerService.getAllCategories();
    }

    public void loadAllCategories(List<Category> categories) {
        ledgerService.deleteCategories();
        categories.forEach(category -> ledgerService.addCategory(category.code(), category.name()));
    }

    public Income addIncome(String args) {
        Income createIncome = transactionMapper.createIncome(args);
        Income savedIncome = ledgerService.addIncome(createIncome);
        log.info("Доход {} добавлен", savedIncome.getId());

        addUndoAction(() -> ledgerService.deleteTransaction(savedIncome));
        return savedIncome;
    }

    public Expense addExpense(String args) {
        Expense newExpense = transactionMapper.createExpense(args);
        boolean isOverLimit = isBudgetOverlimit(newExpense);
        if (isOverLimit) {
            throw new ValidationException("Расход не может быть добавлен из-за превышения бюджета");
        }
        Expense savedExpense = ledgerService.addExpense(newExpense);
        log.info("Расход {} добавлен", savedExpense.getId());

        addUndoAction(() -> ledgerService.deleteTransaction(savedExpense));
        return savedExpense;
    }

    public List<Transaction> getAllTransactions() {
        return ledgerService.getAllTransactions();
    }

    public void loadTransactions(List<Transaction> transactions) {
        ledgerService.deleteTransactions();
        transactions.forEach(transaction -> {
            if (transaction instanceof Income income) {
                ledgerService.addIncome(income);
            } else if (transaction instanceof Expense expense) {
                ledgerService.addExpense(expense);
            } else {
                log.warn("Тип транзакции для загрузки не определён");
            }
        });
    }

    public Budget setBudget(String args) {
        Budget newBudget = budgetMapper.createBudget(args);
        if (budgetService.findByPeriodAndCategory(newBudget.period(), newBudget.category()).isPresent()) {
            log.warn("Бюджет уже установлен для категории {} в период {}", newBudget.category(), newBudget.period());
            throw new ValidationException(
                    "Бюджет уже установлен для категории " + newBudget.category() +
                            " в период " + newBudget.period());
        }
        return budgetService.setBudget(newBudget);
    }

    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    public void loadBudgets(List<Budget> budgets) {
        budgetService.deleteAllBudgets();
        budgets.forEach(budgetService::setBudget);
    }

    public Map<Category, BigDecimal> getReportByMonth(String period) {
        YearMonth month = YearMonth.parse(period);
        return reportService.getSummary(month);
    }

    public List<Expense> getReportTopExpenses(int count) {
        return reportService.topExpenses(count);
    }

    public ImportResult importCsv(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Путь импортируемого файла не должен быть пустым");
        }

        return csvImporter.importCsv(path);
    }

    public void exportReport(String args) {
        if (args == null || args.isEmpty()) {
            throw new IllegalArgumentException("Команда для экспорта не должна быть пустой");
        }

        String[] parts = args.split(" ");

        String format = parts[0];
        ReportExporter reportExporter = ExporterFactory.getExporter(format);

        YearMonth period = YearMonth.parse(parts[1]);
        Path path = Paths.get(parts[2]);

        List<ExportReportItem> reportItems = reportService.getReportRowsForExport(period);
        reportExporter.export(reportItems, path);
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Нет операций для отмены");
            return;
        }

        Runnable undoAction = undoStack.pop();
        undoAction.run();
    }

    private boolean isBudgetOverlimit(Expense expense) {
        if (expense == null) {
            return false;
        }
        YearMonth period = YearMonth.from(expense.getDate());
        Category category = expense.getCategory();
        Budget findBudget = budgetService.findByPeriodAndCategory(period, category).orElse(null);

        if (findBudget == null) {
            return false;
        }

        BigDecimal budgetLimit = findBudget.limit().value();
        BigDecimal totalExpenses = ledgerService.findAllExpensesByPeriodAndCategory(period, category).stream()
                .map(Expense::getAmount)
                .map(Money::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal availableLimit = budgetLimit.subtract(totalExpenses);
        return availableLimit.compareTo(expense.getAmount().value()) < 0;
    }

    private void addUndoAction(Runnable action) {
        if (undoStack.size() == 10) {
            undoStack.removeLast();
        }
        undoStack.push(action);
    }
}