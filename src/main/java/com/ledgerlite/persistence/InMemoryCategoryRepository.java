package com.ledgerlite.persistence;

import com.ledgerlite.domain.Category;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCategoryRepository implements Repository<Category> {
    private final Map<String, Category> storage = new ConcurrentHashMap<>();

    @Override
    public Category save(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория для добавления не указана");
        }

        storage.put(category.code(), category);
        return category;
    }

    @Override
    public Optional<Category> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id для поиска категории не может быть пустым");
        }
        return Optional.ofNullable(storage.get(id.toUpperCase()));
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id для удаления категории не может быть пустым");
        }
        return storage.remove(id) != null;
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }
}