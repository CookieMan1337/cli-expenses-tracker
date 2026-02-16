package com.ledgerlite.persistence;

import com.ledgerlite.domain.Budget;
import com.ledgerlite.domain.Category;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryBudgetRepository implements Repository<Budget> {
    private final Map<String, Budget> storage = new ConcurrentHashMap<>();
    private final static Logger log = LoggerFactory.getLogger(InMemoryBudgetRepository.class);

    @Override
    public Budget save(Budget budget) {
        String key = createKey(budget.period(), budget.category().code());
        storage.put(key, budget);
        log.info("Бюджет {} сохранён в хранилище", key);
        return budget;
    }

    @Override
    public Optional<Budget> findById(String id) {
        return Optional.ofNullable(storage.get(id.toUpperCase()));
    }

    @Override
    public List<Budget> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean delete(String id) {
        return storage.remove(id.toUpperCase()) != null;
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    private String createKey(YearMonth period, String categoryCode) {
        return period + "_" + categoryCode.toUpperCase();
    }

    public Optional<Budget> findByPeriodAndCategory(YearMonth period, Category category) {
        return findAll().stream()
                .filter(budget -> category.code().equals(budget.category().code()))
                .filter(budget -> period.equals(budget.period()))
                .findFirst();
    }
}