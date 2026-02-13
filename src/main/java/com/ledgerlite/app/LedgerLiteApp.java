package com.ledgerlite.app;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.ImportException;
import com.ledgerlite.exception.ValidationException;
import com.ledgerlite.io.CsvExporter;
import com.ledgerlite.io.CsvImporter;
import com.ledgerlite.io.JsonExporter;
import com.ledgerlite.persistence.*;
import com.ledgerlite.report.PeriodSummary;
import com.ledgerlite.report.ReportFormatter;
import com.ledgerlite.service.*;
import com.ledgerlite.util.DateParser;
import com.ledgerlite.util.MoneyParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LedgerLiteApp {
    private final LedgerService ledgerService;
    private final Scanner scanner;
    private boolean running = true;
    private final BudgetService budgetService;
    private final ReportService reportService;
    private final FileStorage fileStorage;
    private final CsvExporter csvExporter;
    private final CsvImporter csvImporter;
    private final ReportFormatter reportFormatter;
    private final JsonExporter jsonExporter;
    private static final Logger log = LoggerFactory.getLogger(LedgerLiteApp.class);
    private UUID lastAddedTransactionId = null;

    public LedgerLiteApp() {
        Repository<Transaction, UUID> transactionRepo =
                new InMemoryRepository<>(Transaction::getId);
        Repository<Category, String> categoryRepo =
                new InMemoryRepository<>(Category::code);
        Repository<Budget, String> budgetRepo =
                new InMemoryRepository<>(Budget::getId);

        this.ledgerService = new LedgerService(transactionRepo, categoryRepo);
        this.budgetService = new BudgetService(budgetRepo, transactionRepo);
        this.reportService = new ReportService(transactionRepo, categoryRepo);
        this.scanner = new Scanner(System.in);
        this.fileStorage = new FileStorage();
        this.csvExporter = new CsvExporter();
        this.reportFormatter = new ReportFormatter();
        this.jsonExporter = new JsonExporter();
        this.csvImporter = new CsvImporter(ledgerService);
        try {
            // Загружаем категории
            List<Category> savedCategories = fileStorage.loadCategories();
            if (!savedCategories.isEmpty()) {
                savedCategories.forEach(categoryRepo::save);
            } else {
                // Если нет сохранённых категорий, добавляем дефолтные
                for (Category cat : Category.defaultCategories()) {
                    categoryRepo.save(cat);
                }
            }

            // Загружаем транзакции
            List<Transaction> savedTransactions = fileStorage.loadTransactions();
            if (!savedTransactions.isEmpty()) {
                savedTransactions.forEach(transactionRepo::save);
            }

            // Загружаем бюджеты
            List<Budget> savedBudgets = fileStorage.loadBudgets();
            if (!savedBudgets.isEmpty()) {
                savedBudgets.forEach(budgetRepo::save);
            }

        } catch (IOException e) {
            System.out.println("Ошибка загрузки данных: " + e.getMessage());
            // При ошибке загрузки создаём дефолтные категории
            for (Category cat : Category.defaultCategories()) {
                categoryRepo.save(cat);
            }
        }
    }

    public void run() {
        log.info("Приложение запущено, ожидание команд пользователя");
        System.out.println("=== LedgerLite - Personal Finance Tracker ===");
        System.out.println("Напишите 'help' для списка доступных команд");
        System.out.println("===========================================");

        while(running) {
            System.out.print("\n > ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            processCommand(input);
        }
        log.info("Приложение завершено");
    }

    private void processCommand(String input){
        Command cmd = Command.fromString(input);

        log.debug("Выполняется команда: {} (ввод: '{}')", cmd, input);

        try{
            switch (cmd) {
                case HELP -> showHelp();
                case ADD_INCOME -> addIncome();
                case ADD_EXPENSE -> addExpense();
                case LIST -> listTransactions();
                case BALANCE -> showBalance();
                case ADD_CATEGORY -> addCategory();
                case LIST_CATEGORY -> listCategories();
                case REMOVE -> removeTransaction();
                case BUDGET_SET -> setBudget();
                case BUDGET_LIST -> listBudgets();
                case REPORT_MONTH -> showMonthReport();
                case EXPORT_CSV -> ExportCSVMonth();
                case EXPORT_JSON -> exportMonthlyReportJSON();
                case EXPORT_TOP_CSV -> exportTopExpenses();
                case REPORT_TOP -> showTopExpenses();
                case IMPORT_CSV -> importCsv();
                case EXIT -> exit();
                default -> {
                    log.warn("Неизвестная команда: {}", input);
                    System.out.println("Неизвестная команда. Напишите 'help' для списка комманд.");
                }
            }
        }catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            System.out.println("Ошибки валидации: " + e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при выполнении команды {}: {}", cmd, e.getMessage(), e);
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private void addIncome(){
        System.out.println("===Добавление дохода===");
        System.out.print("Введите сумму (руб) :");
        String amountStr = scanner.nextLine();
        Money amountIncome = MoneyParser.parse(amountStr);

        System.out.println("Доступные категории: " + ledgerService.getAllCategories());
        System.out.print("Введите категорию: ");
        String categoryCode = scanner.nextLine().toUpperCase();
        if (categoryCode.trim().isEmpty()){
            categoryCode = "OTHER";
        }
        Category category = ledgerService.getCategory(categoryCode)
                .orElseThrow(()-> new ValidationException("Категория не найдена: "));

        System.out.print("Введите дату в формате YYYY-MM-DD или нажмите Entre если дата сегодняшняя: ");
        String dateStr = scanner.nextLine();
        LocalDate date = DateParser.parseOrDefault(dateStr, LocalDate.now());

        System.out.print("Заметки (необязательно): ");
        String note = scanner.nextLine();

        var income = ledgerService.addIncome(date, amountIncome, category, note);
        lastAddedTransactionId = income.getId();
        System.out.printf("Добавлен доход: %s \n",
                income.getAmount());
    }

    private void addExpense() {
        System.out.println("===Добавление трат===");
        System.out.print("Введите сумму (руб) :");
        String amountStr = scanner.nextLine();
        Money amountExpense = MoneyParser.parse(amountStr);

        System.out.print("Доступные категории: ");
        listCategories();
        System.out.print("\nВведите категорию: ");
        String categoryCode = scanner.nextLine().toUpperCase();
        if (categoryCode.trim().isEmpty()){
            categoryCode = "OTHER";
        }
        Category category = ledgerService.getCategory(categoryCode)
                .orElseThrow(()-> new ValidationException("Категория не найдена. "));

        System.out.print("Введите дату в формате YYYY-MM-DD или нажмите Entre если дата сегодняшняя: ");
        String dateStr = scanner.nextLine();
        LocalDate date = DateParser.parseOrDefault(dateStr, LocalDate.now());

        System.out.print("Заметки (необязательно): ");
        String note = scanner.nextLine();

        var expense = ledgerService.addExpense(date, amountExpense, category, note);
        lastAddedTransactionId = expense.getId();
        System.out.printf("Добавлена трата: %s \n",
                expense.getAmount());

        //Контроль превышения бюджета
        YearMonth period = YearMonth.from(date);
        if (budgetService.isBudgetExceeded(period, category)) {
            System.out.println("ВНИМАНИЕ! Бюджет на категорию '" +
                    category.name() + "' за " + period + " ПРЕВЫШЕН!");

            // Показываем детали
            var info = budgetService.getBudgetInfo(period, category);
            System.out.printf("   Лимит: %.2f руб, Потрачено: %.2f руб\n",
                    info.budget().limit().value(),
                    info.spent().value());
        }

    }

    private void listTransactions() {
        System.out.println("===Все транзакции===");

        var transactions = ledgerService.getAllTransactions();
        if (transactions.isEmpty()){
            System.out.println("Транзакций пока не было.");
        } else {
            for (var transaction : transactions) {
                System.out.printf("%s | %s | %s | %s | %s\n",
                        transaction.getId().toString().substring(0, 8),
                        transaction.getDate(),
                        transaction.getType(),
                        transaction.getCategory().name(),
                        transaction.getAmount(),
                        transaction.getNote()
                );
            }
            System.out.printf("\nВсего: %d транзикций\n", transactions.size());
        }
    }

    private void showBalance() {
            Money balance = ledgerService.getBalance();
            Money totalIncome = ledgerService.getTotalIncome();
            Money totalExpense = ledgerService.getTotalExpense();

            System.out.println("===Баланс===");
            System.out.println("Общие доходы:  " + totalIncome);
            System.out.println("Общие расходы: " + totalExpense);
            System.out.println("Баланс: " + balance);

            if (balance.isNegative()) {
                System.out.println("WARNING: Баланс отрицательный!");
            }
        }

    private void addCategory(){
            System.out.println("===Добавление категории===");

            System.out.print("Code (3-6 символов): ");
            String code = scanner.nextLine().trim().toUpperCase();

            System.out.print("Название: ");
            String name = scanner.nextLine().trim();

            var category = ledgerService.addCategory(code, name);
            System.out.printf("Категория добавлена: %s (%s)\n", category.name(), category.code());
    }

    private void listCategories(){
            System.out.println("===Категории===");

            var categories = ledgerService.getAllCategories();
            for (var category : categories) {
                System.out.printf("%-10s %s\n", category.code(), category.name());
            }
    }

    private void removeTransaction(){
        System.out.println("Хотите удалить последнюю транзакцию? y/n");
        String ansv = scanner.nextLine().trim().toLowerCase();
        if (ansv.equals("y")){
            if (lastAddedTransactionId == null){
                System.out.println("В текущей сессии нет транзакций или последняя уже была удалена.");
            } else{
                try{
                    ledgerService.removeTransaction(lastAddedTransactionId);
                    log.info("Транзакция удалена: {}", lastAddedTransactionId);
                    System.out.println("Последняя транзакия удалена");
                    lastAddedTransactionId = null;
                } catch (Exception e){
                    log.error("Ошибка при удалении транзакции: {}", e.getMessage());
                    System.out.println("Ошибка при удалении транзакции: " + lastAddedTransactionId);
                }
            }
            return;
        }
        System.out.print("Введите ID транзакции для удаления: ");
        String id = scanner.nextLine().trim();

        try{
            ledgerService.removeTransaction(id);
            log.info("Транзакция удалена: {}", id);
            System.out.println("Последняя транзакия удалена");
        } catch (Exception e){
            log.error("Ошибка при удалении транзакции: {}", e.getMessage());
            System.out.println("Ошибка при удалении транзакции: " + id);
        }
    }

    private void setBudget() {
        System.out.println("===Установка бюджета===");
        // Показываем категории
        listCategories();

        System.out.print("Код категории: ");
        String categoryCode = scanner.nextLine().toUpperCase();
        Category category = ledgerService.getCategory(categoryCode)
                .orElseThrow(() -> new ValidationException("Категория не найдена"));

        System.out.print("Сумма бюджета (руб): ");
        Money limit = MoneyParser.parse(scanner.nextLine());

        System.out.print("Месяц (ГГГГ-ММ, Enter для текущего): ");
        String monthStr = scanner.nextLine();
        YearMonth period = monthStr.trim().isEmpty()
                ? YearMonth.now()
                : YearMonth.parse(monthStr);

        Budget budget = budgetService.setBudget(period, category, limit);
        System.out.printf("Бюджет установлен: %s - %.2f руб (%s)\n",
                budget.category().name(),
                budget.limit().value(),
                budget.period());
    }

    private void listBudgets() {
        System.out.println("===Все бюджеты===");

        List<Budget> budgets = budgetService.getAllBudgets();
        if (budgets.isEmpty()) {
            System.out.println("Бюджеты не установлены");
            return;
        }

        //Рисуем первую строку таблицы (шапка)
        System.out.printf("%-10s %-20s %10s %12s %s\n",
                "Месяц", "Категория", "Лимит", "Потрачено", "Статус");
        System.out.println("-".repeat(60));

        for (Budget budget : budgets) {
            YearMonth period = budget.period();
            Category category = budget.category();

            var info = budgetService.getBudgetInfo(period, category);
            String status;
            if (info.exceeded()) {
                status = "❌ ПРЕВЫШЕН";
            } else if (info.hasBudget()) {
                status = "✅ В норме";
            } else {
                status = "—";
            }

            System.out.printf("%-10s %-20s %10.2f %12.2f %s\n",
                    period,
                    category.name(),
                    budget.limit().value(),
                    info.spent().value(),
                    status);
        }
    }

    private void showMonthReport() {
        System.out.println("===ОТЧЁТ ЗА ТЕКУЩИЙ МЕСЯЦ===");
        var summary = reportService.getCurrentMonthSummary();
        System.out.println(reportFormatter.format(summary));
    }

    private void showTopExpenses() {
        System.out.println("===ТОП-10 РАСХОДОВ===");
        var top = reportService.getTopExpenses(10);

        if (top.isEmpty()) {
            System.out.println("Нет расходов.");
            return;
        }

        for (int i = 0; i < top.size(); i++) {
            Transaction t = top.get(i);
            System.out.printf("%d. %s | %s | %s | %s%n",
                    i + 1,
                    t.getDate(),
                    t.getCategory().name(),
                    t.getAmount(),
                    t.getNote() != null ? t.getNote() : ""
            );
        }
    }

    private void ExportCSVMonth() {
        System.out.println("===Экспорт в CSV отчет за месяц===");

        try {
            //Получаем данные отчёта
            PeriodSummary summary = reportService.getCurrentMonthSummary();

            csvExporter.exportCurrentMonthReport(summary);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void exportMonthlyReportJSON() {
        try {
            System.out.println("=== ЭКСПОРТ МЕСЯЧНОГО ОТЧЁТА В JSON ===");

            PeriodSummary summary = reportService.getCurrentMonthSummary();
            jsonExporter.exportMonthlyReport(summary);

        } catch (Exception e) {
            log.error("Ошибка JSON экспорта", e);
        }
    }

    private void showHelp(){
        System.out.println(Command.getHelp());
    }

    private void exportTopExpenses() {
        try {
            List<Transaction> topExpenses = reportService.getTopExpenses(10);

            if (topExpenses.isEmpty()) {
                System.out.println("Нет расходов для экспорта");
                return;
            }

            csvExporter.exportTopExpenses(topExpenses);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void importCsv() {
        System.out.println("=== ИМПОРТ ИЗ CSV ===");
        System.out.print("Введите путь к CSV файлу: ");

        String filePath = scanner.nextLine().trim();


        log.info("Начало импорта из файла: {}", filePath);
        System.out.println("Чтение файла: " + filePath);

        try {
            csvImporter.importFromFile(filePath);
        } catch (ImportException e) {
            System.out.println("Ошибка импорта: " + e.getMessage());
            log.error("Ошибка импорта из файла {}: {}", filePath, e.getMessage());

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            log.error("Ошибка чтения файла {}: {}", filePath, e.getMessage());
        }
    }

    private void saveData() {
        try {
            // Собираем все данные из сервисов
            List<Transaction> transactions = ledgerService.getAllTransactions();
            List<Category> categories = ledgerService.getAllCategories();
            List<Budget> budgets = budgetService.getAllBudgets();

            // Сохраняем через FileStorage
            fileStorage.saveAll(transactions, categories, budgets);
            System.out.println("✅ Данные сохранены");
        } catch (IOException e) {
            System.out.println("❌ Ошибка сохранения: " + e.getMessage());
        }
    }

    private void exit(){
        saveData();
        running = false;
    }

    public static void main(String[] args) {
        LedgerLiteApp app = new LedgerLiteApp();
        app.run();

    }


}
