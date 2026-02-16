package com.ledgerlite.io;

import com.ledgerlite.domain.Transaction;
import com.ledgerlite.exception.LedgerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CsvExporter {

    /**
     * Сохраняет список транзакций в файл по указанному пути.
     */
    public void export(Collection<Transaction> transactions, String filePath) {
        Path path = Path.of(filePath); // Путь к файлу

        // столбцы таблицы
        String header = "Date,Type,Amount,Category,Note\n";

        // Превращаем каждую транзакцию в строку CSV
        List<String> lines = transactions.stream()
                .map(this::formatTransaction)
                .collect(Collectors.toList());

        try {
            // Создаем файл (или перезаписываем)
            Files.writeString(path, header);// пишем заголовок
            Files.write(path, lines, StandardOpenOption.APPEND); // пишем строки

            System.out.println("Данные успешно экспортированы в: " + path.toAbsolutePath());
        } catch (IOException e) {
            // Если диск переполнен или нет прав доступа — бросаем исключение
            throw new LedgerException("Ошибка при записи в файл: " + filePath, e);
        }
    }

    private String formatTransaction(Transaction t) {
        // Определяем тип
        String type = (t instanceof com.ledgerlite.domain.Expense) ? "EXPENSE" : "INCOME";

        // Формируем строку
        return String.format("%s,%s,%s,%s,\"%s\"",
                t.getDate(),
                type,
                t.getAmount().value(),
                t.getCategory().code(),
                t.getNote().replace("\"", "'") // Заменяем кавычки чтобы не ломать CSV
        );
    }
}