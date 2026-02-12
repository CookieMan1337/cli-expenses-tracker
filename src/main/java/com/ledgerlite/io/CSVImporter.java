package com.ledgerlite.io;


import com.ledgerlite.domain.*;
import com.ledgerlite.exception.*;
import com.ledgerlite.persistence.Repository;
import com.ledgerlite.service.LedgerService;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CSVImporter {

    private static final int EXPECTED_COLUMNS = 6;

    public static void importFile(String csvPath,LedgerService ledgerService, Repository<Transaction> transactionRepository) throws ImportFormatException {
        File file = new File(csvPath);
        if (!file.exists() || !file.isFile()) {
            throw new ImportFormatException("File not found: " + csvPath);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                // Пропускаю пустые строки и комментарии
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Пропускаю заголовок если есть
                if (lineNumber == 1 && line.toUpperCase().startsWith("TYPE")) {
                    continue;
                }
                try {
                    Transaction tx = parseLine(line,ledgerService);
                    transactionRepository.save(tx);
                } catch (ValidationException e) {
                    String msg = String.format(
                            "Error in line %d: %s (data: %s)",
                            lineNumber, e.getMessage(), line);
                    throw new ImportFormatException(msg);
                }
            }
        } catch (IOException e) {
            throw new ImportFormatException("Cannot read the file: " + csvPath);
        }
    }

    private static Transaction parseLine(String line, LedgerService ledgerService) throws ValidationException, ImportFormatException {
        String[] parts = line.split(",");
        if (parts.length < EXPECTED_COLUMNS) {
            throw new ImportFormatException("Wrong csv format");
        }

        int idx = 0;

        String typeStr = parts[idx++].trim().toUpperCase();
        boolean isIncome;
        if ("INC".equals(typeStr) || "INCOME".equals(typeStr)) {
            isIncome = true;
        } else if ("EXP".equals(typeStr) || "EXPENSE".equals(typeStr)) {
            isIncome = false;
        } else {
            throw new ImportFormatException("Wrong transaction type (expecting INC/EXP): " + typeStr);
        }
        // 2. Дата
        LocalDate date;
        try {
            date = LocalDate.parse(parts[idx++].trim());
        } catch (DateTimeParseException e) {
            throw new ImportFormatException("Wrong date format: " + parts[idx-1]);
        }

        // 3. Сумма
        BigDecimal amountValue;
        try {
            amountValue = new BigDecimal(parts[idx++].trim());
        } catch (NumberFormatException e) {
            throw new ImportFormatException("Wrong amount format: " + parts[idx-1]);
        }

        // 4. Валюта
        Currency currency;
        try {
            currency = Currency.getInstance(parts[idx++].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ImportFormatException("Wrong Currency: " + parts[idx-1]);
        }
        // 5. Код категории
        String catCode = parts[idx++].trim();
        Category category;
        try {
            category = ledgerService.getCategoryByCode(catCode);
        } catch (ValidationException e) {
            throw new ImportFormatException("Category not found: " + catCode);
        }

        // 6. Заметка
        String note = "";
        if (idx < parts.length) {
            note = String.join(",", Arrays.copyOfRange(parts, idx, parts.length)).trim();
            // Убираем обрамляющие кавычки если есть
            if (note.startsWith("\"") && note.endsWith("\"")) {
                note = note.substring(1, note.length() - 1);
            }
        }

        Money money = new Money(amountValue, currency);

        if (isIncome) {
            return new Income(date, money, category, note);
        } else {
            return new Expense(date, money, category, note);
        }
    }

}
