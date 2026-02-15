package com.ledgerlite.service;

import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Expense;
import com.ledgerlite.domain.Transaction;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для генерации отчетов
 * Здесь нет хранения данных, только логика обработки
 */
public class ReportService {

    /**
     * 1. Сводка: группируем ВСЕ транзакции по категориям и считаем суммы.
     * Результат: Map (Категория -> Итоговая сумма)
     */
    // Collection<> - признак хорошего тона, принимает любые типы коллекций
    // можно изменить тип Transaction в любой другой и данные методы этого не заметят
    public Map<Category, BigDecimal> calculateSummary(Collection<Transaction> transactions) {
        return transactions.stream() // открываем конвейер
                .filter(t -> t != null && t.getAmount() != null && t.getCategory() != null) //фильтруем поломанные записи
                .filter(t -> t.getAmount().value().compareTo(BigDecimal.ZERO) > 0) // фильтруем записи где сумма = 0
                // далее все группируем и суммируем
                .collect(Collectors.groupingBy( // создаем Map,
                        Transaction::getCategory, // где ключами будут категории
                        // getCategory - геттер из класса транзакций
                        Collectors.reducing( //  редукция, так как нельзя просто складывать BigDecimal
                                // редукция - типа иттератора с накапливанием?
                                BigDecimal.ZERO, // --- Identity --- начинаем с нуля
                                t -> t.getAmount().value(), // --- Mapper --- берем числовое значение из Money
                                BigDecimal::add // --- BinaryOperator --- метод, которым складываем
                        )
                ));
    }

    /**
     * 2. Топ-N расходов: находим самые крупные траты.
     */

    // опять входной тип - Collection
    public List<Transaction> getTopNExpenses(Collection<Transaction> transactions, int n) {
        return transactions.stream()
                .filter(t -> t!= null && t.getAmount() != null) // отсев сломанных записей чтобы дальше ничего не упало
                .filter(t -> t instanceof Expense) // фильтруем по типу, оставляем только расходы
                // Сортировка
                // Сравни две транзакции, чтобы понять, какая больше
                // Сравнивая, возьми каждую t -> и достань из неё числовое значение суммы
                // Еще и задом наперед
                .sorted(Comparator.comparing((Transaction t) -> t.getAmount().value()).reversed())
                .limit(n) // Берем первые N элементов
                .collect(Collectors.toList()); // Собираем результат в список
    }

    /**
     * 3. Фильтрация по периоду (бонус).
     */
    public List<Transaction> getTransactionsByPeriod(Collection<Transaction> transactions, int month, int year) {
        return transactions.stream()
                .filter(t -> t!= null && t.getDate() != null) // фильтруем все что может сломать
                // отфильтровываем месяц
                .filter(t -> t.getDate().getMonthValue() == month)
                .filter(t -> t.getDate().getYear() == year)
                // тут вроде как если числа будут неадекватными (15 месяц или 45398 год)
                // то просто вернется пустой список
                .collect(Collectors.toList());
    }
}