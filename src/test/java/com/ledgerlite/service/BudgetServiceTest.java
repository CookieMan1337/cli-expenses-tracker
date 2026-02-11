package com.ledgerlite.service;

import com.ledgerlite.domain.Budget;
import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Money;
import com.ledgerlite.domain.Transaction;
import com.ledgerlite.persistence.FileStore;
import com.ledgerlite.persistence.InMemoryRepository;
import com.ledgerlite.persistence.Repository;
import com.ledgerlite.service.BudgetService;
import com.ledgerlite.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;


class BudgetServiceTest {
    private final Repository<Transaction> transactionRepository = new InMemoryRepository<>();
    private final Map<YearMonth,Map<Category,Budget>> budgets = new ConcurrentHashMap<>();
    private LedgerService ledgerService;
    private BudgetService budgetService;

    private final FileStore fileStore = new FileStore();

    private final Currency RUB = Currency.getInstance("RUB");
    private Category food;
    private Category entertainment;

    @BeforeEach
    void setUp(){
        ledgerService = new LedgerService(transactionRepository,budgets,fileStore);
        budgetService = new BudgetService(budgets,ledgerService);


        food = new Category("FOOD", "Food");
        ledgerService.addCategory(food.code(),food.name());

        entertainment = new Category("ENTERT", "Entertainment");
        ledgerService.addCategory(entertainment.code(),entertainment.name());

    }

    @Test
    void testSetBudget() {
        ledgerService.setBudget(YearMonth.of(2026,1), food.code(), new Money(BigDecimal.valueOf(400),RUB));
        int all = budgets.size();
        assertEquals(1,all);
        Map<Category,Budget> saved = budgets.get(YearMonth.of(2026,1));
        Boolean cat = saved.containsKey(food);
        assertEquals(true, cat);
        assertEquals(new BigDecimal("400.00"), saved.get(food).limit().value());
        assertTrue(saved.containsKey(food));
    }

    @Test
    void testIsExceed() {
        ledgerService.setBudget(YearMonth.of(2026,1), food.code(), new Money(BigDecimal.valueOf(500),RUB));
        ledgerService.addExpense(LocalDate.of(2026,1,20),new Money(BigDecimal.valueOf(1000), RUB),food, "еда");
        boolean result = budgetService.isExceed(YearMonth.of(2026,1),food.code());
        assertTrue(result);
    }

    @Test
    void testNotExceed() {
        ledgerService.setBudget(YearMonth.of(2026,1), food.code(), new Money(BigDecimal.valueOf(500),RUB));
        ledgerService.addExpense(LocalDate.of(2026,1,20),new Money(BigDecimal.valueOf(200), RUB),food, "еда");
        boolean result = budgetService.isExceed(YearMonth.of(2026,1),food.code());
        assertFalse(result);
    }


    @Test
    void returnFalseForNonExistingBudget() {
        assertFalse(budgetService.isExceed(YearMonth.of(2026,1),food.code()));
    }


    //// убрала Repository<Category> categoryRepository отовсюду, мб добавить в интерфейс отдельные методы для категорий и бюджета
}

