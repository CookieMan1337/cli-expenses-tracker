package com.ledgerlite.io;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.ImportException;
import com.ledgerlite.service.LedgerService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CsvImporter {

    private final LedgerService ledgerService;

    public CsvImporter(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }




    public void importFromFile(String filePath) throws IOException, ImportException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new ImportException("Файл не найден: " + filePath);
        }

        int successCount = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) continue;

                // Пропускаем заголовок
                if (lineNumber==1) {
                    continue;
                }

                try {
                    // Парсим строку и создаём транзакцию
                    Transaction transaction = parseLine(line);

                    // Сохраняем
                    if (transaction instanceof Income) {
                        ledgerService.addIncome(
                                transaction.getDate(),
                                transaction.getAmount(),
                                transaction.getCategory(),
                                transaction.getNote()
                        );
                    } else  if (transaction instanceof Expense){
                        ledgerService.addExpense(
                                transaction.getDate(),
                                transaction.getAmount(),
                                transaction.getCategory(),
                                transaction.getNote()
                        );
                    } else {throw new Exception();}

                    successCount++;

                } catch (Exception e) {
                    throw new ImportException(
                            String.format("Ошибка в строке %d: %s", lineNumber, e.getMessage())
                    );
                }
            }
        }

        System.out.println("Файл успешно импортирован, добавлено транзакций: " + successCount);
    }

    private Transaction parseLine(String line) throws ImportException {
        String[] parts = line.split("[,;]");

        if (parts.length < 4) {
            throw new ImportException("Недостаточно полей. Ожидается от 4");
        }

        try {
            // 1. Дата
            LocalDate date = LocalDate.parse(parts[0].trim());

            // 2. Тип
            String typeStr = parts[1].trim().toLowerCase();
            boolean isIncome;
            if (typeStr.equals("income")) {
                isIncome = true;
            } else if (typeStr.equals("expense")) {
                isIncome = false;
            } else {
                throw new ImportException("Неизвестный тип: " + typeStr);
            }

            // 3. Категория
            String categoryCode = parts[2].trim().toUpperCase();
            Category category = ledgerService.getCategory(categoryCode)
                    .orElseThrow(() -> new ImportException("Категория не найдена: " + categoryCode));

            // 4. Сумма
            double amountValue = Double.parseDouble(parts[3].trim());
            if (amountValue <= 0) {
                throw new ImportException("Сумма должна быть положительной");
            }
            Money amount = Money.of(amountValue, "RUB");

            // 5. Заметка (опционально)
            String note = parts.length >= 5 ? parts[4].trim() : "";

            // Создаём транзакцию
            if (isIncome) {
                return new Income(date, amount, category, note);
            } else {
                return new Expense(date, amount, category, note);
            }

        } catch (DateTimeParseException e) {
            throw new ImportException("Неверный формат даты. Используйте ГГГГ-ММ-ДД");
        } catch (NumberFormatException e) {
            throw new ImportException("Неверный формат суммы");
        }
    }
}