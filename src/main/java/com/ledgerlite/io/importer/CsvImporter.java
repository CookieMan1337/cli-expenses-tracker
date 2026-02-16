package com.ledgerlite.io.importer;

import com.ledgerlite.domain.Expense;
import com.ledgerlite.domain.Income;
import com.ledgerlite.service.LedgerService;
import com.ledgerlite.util.TransactionMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CsvImporter {
    private final LedgerService ledgerService;
    private final TransactionMapper transactionMapper;

    public CsvImporter(LedgerService ledgerService, TransactionMapper transactionMapper) {
        this.ledgerService = ledgerService;
        this.transactionMapper = transactionMapper;
    }

    public ImportResult importCsv(String path) {
        Path csvPath = Paths.get(path);
        ImportResult importResult = new ImportResult();
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                importResult.incrementTotalRows();
                if (line.trim().isBlank()) {
                    continue;
                }

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 4) {
                    importResult.incrementFailed();
                    continue;
                }
                try {
                    String type = parts[1];
                    String args = getTransactionString(parts);
                    if (type.equals("EXPENSE")) {
                        Expense newExpense = transactionMapper.createExpense(args);
                        Expense savedExpense = ledgerService.addExpense(newExpense);
                        log.trace("Во время импорта в хранилище добавлен расход {}", savedExpense.getId());
                        importResult.incrementSuccessful();
                    } else if (type.equals("INCOME")) {
                        Income newIncome = transactionMapper.createIncome(args);
                        Income savedIncome = ledgerService.addIncome(newIncome);
                        log.trace("Во время импорта в хранилище добавлен доход {}", savedIncome.getId());
                        importResult.incrementSuccessful();
                    } else {
                        importResult.incrementFailed();
                        log.warn("Во время импорта транзакция не добавлена из-за неизвестного типа операции ");
                    }
                } catch (Exception exception) {
                    importResult.incrementFailed();
                    log.error("Во время импорта произошла ошибка во время обработки строчки файла: {}",
                            exception.getMessage());
                }
            }
        } catch (IOException exception) {
            log.error("Во время импорта произошла ошибка чтения файла {}: {}",
                    path, exception.getMessage());
            throw new RuntimeException("Во время импорта произошла ошибка чтения файла: " + path);
        }
        return importResult;
    }

    public String getTransactionString(String[] parts) {
        String date = parts[0];
        String amount = parts[2];
        String categoryCode = parts[3];
        String note = parts.length > 4 ? parts[4] : "";

        return String.join(" ", date, amount, categoryCode, note);
    }
}