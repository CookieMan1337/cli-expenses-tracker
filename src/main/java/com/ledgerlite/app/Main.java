package com.ledgerlite.app; // Объявление пакета

import com.ledgerlite.domain.*;
import com.ledgerlite.persistence.*;
import com.ledgerlite.service.ReportService;
import com.ledgerlite.exception.LedgerException;
import com.ledgerlite.io.CsvExporter;

// импорты для планировщика
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;


public class Main {
    // Глобальные константы для путей
    private static final String DATA_FOLDER = "data";
    private static final String AUTOSAVE_NAME = "autosave.csv";
    private static final String IMPORT_NAME = "statement_import.csv";

    private static final Path AUTOSAVE_PATH = Paths.get(DATA_FOLDER, AUTOSAVE_NAME);
    private static final Path IMPORT_PATH = Paths.get(DATA_FOLDER, IMPORT_NAME);
    private static final Path IMPORT_PATH = Paths.get(DATA_FOLDER, IMPORT_NAME);

    // Инициализируем компоненты

    //1. Хранение
    private static final Repository<Transaction, UUID> transactionRepo =
            new InMemoryRepository<>(Transaction::getId);
    private static final Repository<Category, UUID> categoryRepo =
            new InMemoryRepository<>(Category::id);

    // 2. Сервис для генерации отчетов
    private static final ReportService reportService = new ReportService();

    // 3. Стек (LIFO) для хранения ID транзакций
    private static final Deque<UUID> undoStack = new ArrayDeque<>();

    //4. Логирование
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // 5. CsvExporter
    private static final CsvExporter exporter = new CsvExporter();

    // 6. Планировщик
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 7. Сканер
    private static Scanner scanner;


    public static void main(String[] args) {

        System.out.println("=== LedgerLite: запуск ===");
        startAutoSave();

        scanner = new Scanner(System.in); // ввод из консоли
        System.out.println("=== LedgerLite: Автосохранение включено ===");

        System.out.println("=== LedgerLite CLI приветствует вас! ===");

        // Бесконечный цикл для работы программы до команды exit
        while (true) {
            System.out.println("\nДоступные действия: [add] Добавить, [report] Отчет, [undo] Отмена, [exit] Выход");
            System.out.print("Выберите команду: ");
            String command = scanner.nextLine().toLowerCase().trim();

            if (command.isEmpty()) continue; // игнорируем случайный Enter

            try {
                switch (command) {
                    case "add" -> startAddWizard();
                    case "report" -> handleReport();
                    case "undo" -> handleUndo();
                    case "exit" -> {
                        scheduler.shutdown();
                        System.out.println("=== Планировщик выключен ===");
                        System.out.println("=== Программа завершена. Хорошего дня! ===");
                        return;
                    }
                    default -> System.out.println("Неизвестная команда. Попробуйте еще раз.");
                }
            } catch (LedgerException e) {
                // Обработка ошибок бизнес-логики
                System.err.println("Ошибка: " + e.getMessage()); // (хз пока каких)
            } catch (Exception e) {

                System.err.println("Произошла ошибка: " + e.getMessage());
            }
        }
    }
    /**
     * Визард для добавления транзакции
     * Я забыла про формат команд из требований и сделала красивые визарды
     */
    private static void startAddWizard() {
        System.out.println("=== Добавление новой записи ===");

        // 1. Выбор типа
        System.out.print("Тип транзакции: доход или расход ( inc | exp ): ");
        String type = scanner.nextLine().trim().toLowerCase();
        boolean isIncome = type.equals("inc");

        // 2. Ввод суммы
        BigDecimal amount = readBigDecimal("Введите сумму: ");

        // 3. Ввод даты
        LocalDate today = LocalDate.now();
        String prompt = String.format(
                "Введите дату в формате ГГГГ-ММ-ДД (по умолчанию %s): ",
                today
        );
        LocalDate date = readDate(prompt);

        // 4. Категория
        System.out.println("Доступные категории:");

        categoryRepo.findAll().forEach(cat ->
                System.out.printf(cat.code() + " - " + cat.name())
        );

        System.out.println("Введите код категории или придумайте новый:");
        String catCode = scanner.nextLine().trim().toUpperCase();

        Category category = categoryRepo.findAll().stream()
                .filter(c -> c.code().equalsIgnoreCase(catCode))
                .findFirst()
                .orElse(null);

        if (category != null) {
            System.out.println("Выбрана существующая категория: " + category.name());
        } else {
            System.out.println("Категория с таким кодом не найдена. Создадим новую?");
            System.out.print("Введите название для кода '" + catCode + "': ");
            String newName = scanner.nextLine().trim();

            if (newName.isEmpty()) {
                // Если пользователь поленился вводить имя, ставим дефолт
                category = Category.of("OTHER", "Другое");
                System.out.println("Назначена категория по умолчанию: Другое");
            } else {
                // Создаем и сохраняем новую категорию
                category = Category.of(catCode, newName);
                categoryRepo.save(category); // Не забываем сохранить в репозиторий!
                System.out.println("Создана новая категория: " + newName);
            }
        }


        // 5. Заметка
        System.out.print("Добавьте заметку (опционально): ");
        String note = scanner.nextLine().trim();

        // Финал - создание объекта
        Money money = new Money(amount, Currency.getInstance("RUB"));

        Transaction t = isIncome
                ? new Income(date, money, category, note)
                : new Expense(date, money, category, note);

        transactionRepo.save(t);
        undoStack.push(t.getId());

        System.out.println("=== Запись успешно сохранена! ===");
    }

    /**
     * Метод для отмены последней операции (Undo)
     */
    private static void handleUndo() {
        if (undoStack.isEmpty()) {
            System.out.println("Нечего отменять.");
            return;
        }
        // Удаляем элемент из вершины стека
        UUID lastId = undoStack.pop();

        // Удаляем транзакцию из репозитория по этому ID
        transactionRepo.delete(lastId);
        System.out.println("Последняя операция отменена (удалена транзакция " + lastId + ")");
    }

    /**
     * Метод для вывода отчетов
     */
    private static void handleReport() {
        System.out.println("\n=== Выберите тип отчета ===");
        System.out.println("1. Сводка по категориям");
        System.out.println("2. Топ-N самых крупных расходов");
        System.out.println("3. Фильтр за конкретный месяц/год");
        System.out.print("Введите номер (1-3): ");

        String choice = scanner.nextLine().trim();
        // Получаем все данные один раз, чтобы передавать в методы
        var allData = transactionRepo.findAll();

        if (allData.isEmpty()) {
            System.out.println("Данных для отчета пока нет. Сначала добавьте транзакции.");
            return;
        }

        switch (choice) {
            case "1" -> showSummaryReport(allData);
            case "2" -> showTopNReport(allData);
            case "3" -> showPeriodReport(allData);
            default -> System.out.println("Неверный выбор.");
        }

    }

    private static void showSummaryReport(Collection<Transaction> data) {
        var summary = reportService.calculateSummary(data);
        System.out.println("\n=== Сводка по категориям ===");
        summary.forEach((cat, sum) ->
                System.out.printf(" %-15s : %10.2f USD\n", cat.name(), sum));
    }

    private static void showTopNReport(Collection<Transaction> data) {
        System.out.print("Сколько записей вывести в ТОП?: ");
        int n = readInt();

        var topExpenses = reportService.getTopNExpenses(data, n);

        System.out.println("\n=== ТОП-" + n + " Ваших трат ===");
        if (topExpenses.isEmpty()) {
            System.out.println("Расходов не найдено.");
        } else {
            topExpenses.forEach(t ->
                    System.out.printf("%s | %-10s | %8.2f | %s\n",
                            t.getDate(), t.getCategory().name(), t.getAmount().value(), t.getNote()));
        }
    }

    private static void showPeriodReport(Collection<Transaction> data) {
        System.out.print("Введите год (например, 2026): ");
        int year = readInt();
        System.out.print("Введите месяц (1-12): ");
        int month = readInt();

        var filtered = reportService.getTransactionsByPeriod(data, month, year);

        System.out.printf("\n === Операции за %02d.%d === \n", month, year);
        if (filtered.isEmpty()) {
            System.out.println("За этот период записей нет.");
        } else {
            filtered.forEach(t -> {
                String type = (t instanceof Expense) ? "[-]" : "[+]";
                System.out.printf("%s %s | %-10s | %8.2f | %s\n",
                        type, t.getDate(), t.getCategory().name(), t.getAmount().value(), t.getNote());
            });
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Ошибка! Введите целое число: ");
            }
        }
    }


    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().replace(",", "."); // Заменяем запятую на точку для удобства
                BigDecimal val = new BigDecimal(input);

                if (val.compareTo(BigDecimal.ZERO) <= 0) throw new LedgerException("Введенная сумма меньше или равно нулю");
                return val;

            } catch (NumberFormatException e) {
                // Обрабатываем случай, если ввели текст вместо числа
                System.out.println("Ошибка: Пожалуйста, введите корректное число");
            } catch (LedgerException e) {
                // Выводим сообщение
                System.out.println(e.getMessage());
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return LocalDate.now(); // По умолчанию — сегодня
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: Неверный формат даты. Нужно ГГГГ-ММ-ДД.");
            }
        }
    }

    private static void startAutoSave() {

        log.info("Инициализация службы автосохранения. Интервал: 30 сек.");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Путь к файлу-дампу
                Files.createDirectories(AUTOSAVE_PATH.getParent());

                // Проверяем, есть ли что сохранять
                var data = transactionRepo.findAll();

                if (!data.isEmpty()) {
                    log.debug("Начало процесса автосохранения {} записей...", data.size());
                    exporter.export(data, AUTOSAVE_PATH.toString());
                    log.info("Автосохранение успешно выполнено в: {}", AUTOSAVE_PATH.toAbsolutePath());

                } else {
                    log.trace("Нет данных для автосохранения.");
                }

            } catch (Exception e) {
                log.error("Критическая ошибка при автосохранении: ", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }


}