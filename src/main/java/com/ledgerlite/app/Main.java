package com.ledgerlite.app;

import com.ledgerlite.domain.*;
import com.ledgerlite.io.importer.ImportResult;
import com.ledgerlite.persistence.FileStorage;
import com.ledgerlite.service.ServiceContainer;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Main {
    private final Scanner scanner;
    private final ServiceContainer serviceContainer;

    public Main(ServiceContainer serviceContainer) {
        this.scanner = new Scanner(System.in);
        this.serviceContainer = serviceContainer;
    }

    public static void main(String[] args) {
        log.info("Приложение запущено");
        ServiceContainer serviceContainer = AppConfig.createServiceContainer();

        FileStorage storage = new FileStorage();
        storage.load(serviceContainer);

        Main main = new Main(serviceContainer);
        main.start();
        log.info("Приложение остановлено");
        storage.save(serviceContainer);
    }

    public void start() {
        while (true) {
            printMenu();
            String command = scanner.nextLine();
            switch (command) {
                case "add-cat":
                    addCategory();
                    break;
                case "add-inc":
                    addIncome();
                    break;
                case "add-exp":
                    addExpense();
                    break;
                case "set-budget":
                    setBudget();
                    break;
                case "report month":
                    getReportExpensesByMonth();
                    break;
                case "report top":
                    getReportTopExpenses();
                    break;
                case "import csv":
                    importCsv();
                    break;
                case "export report":
                    exportReport();
                    break;
                case "undo":
                    undo();
                    break;
                case "exit":
                    return;
                default:
                    System.out.println("Команда не известна");
            }
        }
    }

    private void addCategory() {
        System.out.println("Введите код новой категории:");
        String code = scanner.nextLine();
        System.out.println("Введите название новой категории:");
        String name = scanner.nextLine();
        try {
            Category savedCategory = serviceContainer.addCategory(code, name);
            System.out.println("Категория добавлена: " + savedCategory);
        } catch (Exception exception) {
            System.out.println("Не удалось добавить категорию");
            System.out.println(exception.getMessage());
        }
    }

    private void addIncome() {
        System.out.println("Введите доход в корректном формате.");
        System.out.println("Формат: ГГГГ-ММ СУММА КАТЕГОРИЯ [ПРИМЕЧАНИЕ]");

        String income = scanner.nextLine();
        try {
            Income savedIncome = serviceContainer.addIncome(income);
            System.out.println("Доход добавлен: " + savedIncome);
        } catch (Exception exception) {
            System.out.println("Не удалось добавить доход");
            System.out.println(exception.getMessage());
        }
    }

    private void addExpense() {
        System.out.println("Введите расход в корректном формате.");
        System.out.println("Формат: ГГГГ-ММ СУММА КАТЕГОРИЯ [ПРИМЕЧАНИЕ]");

        String expense = scanner.nextLine();
        try {
            Expense savedExpense = serviceContainer.addExpense(expense);
            System.out.println("Расход добавлен: " + savedExpense);
        } catch (Exception exception) {
            System.out.println("Не удалось добавить расход");
            System.out.println(exception.getMessage());
        }
    }

    private void setBudget() {
        System.out.println("Введите бюджет на меня в корректном формате");
        System.out.println("Формат: ГГГГ-ММ КАТЕГОРИЯ ЛИМИТ");

        String budget = scanner.nextLine();
        try {
            Budget savedBudget = serviceContainer.setBudget(budget);
            System.out.println("Бюджет добавлен: " + savedBudget);
        } catch (Exception exception) {
            System.out.println("Не удалось добавить бюджет");
            System.out.println(exception.getMessage());
        }
    }

    private void getReportExpensesByMonth() {
        System.out.println("Введите месяц, за который нужно сформировать отчёт по расходам");
        System.out.println("Формат: ГГГГ-ММ");
        String period = scanner.nextLine();
        try {
            Map<Category, BigDecimal> reportRows = serviceContainer.getReportByMonth(period);

            System.out.println("Отчёт за " + period + ":");
            reportRows.forEach((category, sum) ->
                    System.out.printf("%s: %s руб%n", category.name(), sum)
            );
            System.out.println("-".repeat(30));
        } catch (Exception exception) {
            System.out.println("Не удалось сформировать отчёт по расходам за месяц");
            System.out.println(exception.getMessage());
        }
    }

    private void getReportTopExpenses() {
        System.out.println("Введите количество ТОП расходов, которые хотите увидеть");
        System.out.println("Формат: положительное целое число");
        if (scanner.hasNextInt()) {
            int topCount = scanner.nextInt();
            scanner.nextLine();
            try {
                List<Expense> reportRows = serviceContainer.getReportTopExpenses(topCount);
                System.out.println("ТОП-" + topCount + "расходов:");
                reportRows.forEach((System.out::println));
                System.out.println("-".repeat(30));
            } catch (Exception exception) {
                System.out.println("Не удалось сформировать отчёт по ТОП-" + topCount + " расходам.");
                System.out.println(exception.getMessage());
            }
        } else {
            System.out.println("Необходимо ввести целое число для отображения отчёта");
            scanner.nextLine();
        }
    }

    private void importCsv() {
        System.out.println("Введите путь файла, из которого хотите сделать импорт:");
        String path = scanner.nextLine();
        try {
            ImportResult importResult = serviceContainer.importCsv(path);
            System.out.println("Результат импорта:");
            System.out.println("Всего строк - " + importResult.getTotalRows() + ";\n" + "Успешно добавлены - " +
                    importResult.getSuccessful() + ";\n" + "Не добавлены - " + importResult.getFailed());
        } catch (Exception exception) {
            System.out.println("Не удалось импортировать файл");
            System.out.println(exception.getMessage());
        }
    }

    private void exportReport() {
        System.out.println("Введите параметры отчёта для экспорта");
        System.out.println("Формат: csv|json ГГГГ-ММ ./путь файла ");
        String input = scanner.nextLine();
        try {
            serviceContainer.exportReport(input);
            System.out.println("Отчёт по расходам экспортирован в файл");
        } catch (Exception exception) {
            System.out.println("Не удалось экспортировать файл");
            System.out.println(exception.getMessage());
        }
    }

    private void undo() {
        try {
            serviceContainer.undo();
            System.out.println("Последняя операция отменена");
        } catch (Exception exception) {
            System.out.println("Не удалось отменить последнюю операцию");
            System.out.println(exception.getMessage());
        }
    }


    private static void printMenu() {
        System.out.println("-".repeat(20));
        System.out.println("Выберите команду:");
        System.out.println("add-cat - Добавить категорию");
        System.out.println("add-inc - Добавить доход");
        System.out.println("add-exp - Добавить расход");
        System.out.println("set-budget - Установить бюджет");
        System.out.println("report month - Получить отчёт расходов за месяц");
        System.out.println("report top - Получить ТОП N расходов");
        System.out.println("import csv - Загрузить отчёт");
        System.out.println("export report - Выгрузить отчёт");
        System.out.println("undo - Отменить последнюю операцию");
        System.out.println("exit - Выход");
        System.out.println("-".repeat(20));
    }
}