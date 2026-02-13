package com.ledgerlite.io;

import com.ledgerlite.domain.Money;
import com.ledgerlite.domain.Transaction;
import com.ledgerlite.report.CategoryExpense;
import com.ledgerlite.report.PeriodSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String EXPORTS_DIR = "exports";

    public CsvExporter() {
        createExportsDirectory();
    }

    public void exportCurrentMonthReport(PeriodSummary summary) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
        String fileName = "report_" + timestamp + ".csv";

        Path filePath = Paths.get(EXPORTS_DIR, fileName);

        // Создаём директорию для экспортов, если её нет
        Files.createDirectories(filePath.getParent());

        // Формируем CSV содержимое
        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF");
        // 1. Заголовок и мета-информация (комментариями #)
        csv.append(" ОТЧЁТ ЗА ПЕРИОД\n");
        csv.append(String.format(" Период: %s - %s\n",
                summary.from().format(DATE_FORMATTER),
                summary.to().format(DATE_FORMATTER)));
        csv.append(String.format(" Доходы: %.2f RUB\n",
                summary.totalIncome().value()));
        csv.append(String.format(" Расходы: %.2f RUB\n",
                summary.totalExpense().value()));
        csv.append(String.format(" Баланс: %.2f RUB\n",
                summary.balance().value()));
        csv.append(String.format(" Всего транзакций: %d\n\n",
                summary.transactionCount()));

        // 2. Заголовки таблицы
        csv.append("Категория;Сумма (RUB);Процент\n");

        // 3. Данные по категориям
        for (CategoryExpense ce : summary.topCategories()) {
            double percent = calculatePercent(ce.amount(), summary.totalExpense());
            csv.append(String.format("%s;%.2f;%.1f%%\n",
                    escapeCsv(ce.category().name()),
                    ce.amount().value(),
                    percent));
        }

        // Записываем файл
        Files.writeString(filePath, csv.toString());

        System.out.println("Файл сохранён: " + filePath.toAbsolutePath());
    }


    public void exportTopExpenses(List<? extends Transaction> transactions) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
        String fileName = "reportTop10_" + timestamp + ".csv";

        Path filePath = Paths.get(EXPORTS_DIR, fileName);
        Files.createDirectories(filePath.getParent());

        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF");

        csv.append(" ТОП-10 РАСХОДОВ\n");
        csv.append(" №;Дата;Категория;Сумма;Описание\n");

        int rank = 1;
        for (Transaction t : transactions) {
            csv.append(String.format("%d;%s;%s;%.2f;%s\n",
                    rank++,
                    t.getDate().format(DATE_FORMATTER),
                    escapeCsv(t.getCategory().name()),
                    t.getAmount().value(),
                    escapeCsv(t.getNote() != null ? t.getNote() : "")));
        }

        Files.writeString(filePath, csv.toString());
        System.out.println("Файл сохранён: " + filePath.toAbsolutePath());
    }

    private double calculatePercent(Money part, Money total) {
        if (total.value().doubleValue() == 0) return 0;
        return part.value()
                .divide(total.value(), 4, java.math.RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }

    //Экранирование для csv
    private String escapeCsv(String value) {
        if (value == null) return "";

        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void createExportsDirectory() {
        try {
            Files.createDirectories(Paths.get(EXPORTS_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать папку для экспортов: " + e.getMessage());
        }
    }
}
