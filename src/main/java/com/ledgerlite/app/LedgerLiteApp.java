package com.ledgerlite.app;

import com.ledgerlite.domain.Budget;
import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Money;
import com.ledgerlite.domain.Transaction;
import com.ledgerlite.exception.ValidationException;
import com.ledgerlite.persistence.InMemoryRepository;
import com.ledgerlite.persistence.Repository;
import com.ledgerlite.service.BudgetService;
import com.ledgerlite.service.LedgerService;
import com.ledgerlite.service.ReportService;
import com.ledgerlite.util.DateParser;
import com.ledgerlite.util.MoneyParser;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;


public class LedgerLiteApp {
    private final LedgerService ledgerService;
    private final Scanner scanner;
    private boolean running = true;
    private final BudgetService budgetService;
    private final ReportService reportService;

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
    }

    public void run() {
        System.out.println("=== LedgerLite - Personal Finance Tracker ===");
        System.out.println("Напишите 'help' для списка доступных команд");
        System.out.println("===========================================");

        while(running) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            processCommand(input);
        }
    }

    private void processCommand(String input){
        Command cmd = Command.fromString(input);

        try{
            switch (cmd) {
                case HELP -> showHelp();
                case ADD_INCOME -> addIncome();
                case ADD_EXPENSE -> addExpense();
                case LIST -> listTransactions();
                case BALANCE -> showBalance();
                case ADD_CATEGORY -> addCategory();
                case LIST_CATEGORY -> listCategories();
                case REPORT_MONTH -> showMonthReport();
                case REPORT_TOP -> showTopExpenses();
                case REMOVE -> removeTransaction();
                case EXIT -> exit();
                default -> System.out.println("Unknown command. Type 'help' for commands.");
            }
        }catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private void showHelp(){
        System.out.println(Command.getHelp());
    }

    private void addIncome(){
        System.out.println("===Добавление дохода===");
        System.out.print("Введите сумму (руб) :");
        String amountStr = scanner.nextLine();
        Money amountIncome = MoneyParser.parse(amountStr);

        System.out.println("Доступные категории: " + ledgerService.getAllCategories());
        System.out.print("Введите категорию: ");
        String categoryCode = scanner.nextLine();
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
        System.out.printf("Добавлен доход: %s (ID: %s)\n",
                income.getAmount(), income.getId());
    }

    private void addExpense() {
        System.out.println("===Добавление трат===");
        System.out.print("Введите сумму (руб) :");
        String amountStr = scanner.nextLine();
        Money amountExpense = MoneyParser.parse(amountStr);

        System.out.println("Доступные категории: " + ledgerService.getAllCategories());
        System.out.print("Введите категорию: ");
        String categoryCode = scanner.nextLine();
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

        var income = ledgerService.addExpense(date, amountExpense, category, note);
        System.out.printf("Добавлена трата: %s (ID: %s)\n",
                income.getAmount(), income.getId());
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
            System.out.printf("\nTotal: %d transactions\n", transactions.size());
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

    private void showMonthReport() {
        System.out.println("===ОТЧЁТ ЗА ТЕКУЩИЙ МЕСЯЦ===");
        var summary = reportService.getCurrentMonthSummary();
        System.out.println(summary);
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

    private void removeTransaction(){
            System.out.print("Введите ID транзакции для удаления: ");
            String id = scanner.nextLine().trim();

            ledgerService.removeTransaction(id);
            System.out.println("Транзакция удалена.");
    }

    private void exit(){
        running = false;
    }

    public static void main(String[] args) {
        LedgerLiteApp app = new LedgerLiteApp();
        app.run();

    }


}
