package com.ledgerlite.persistence;

import com.ledgerlite.exception.EntityNotFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryRepository<T, ID> implements Repository<T, ID> {
    private final Map<ID, T> storage = new ConcurrentHashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public T save(T entity) {
        ID id = idExtractor.apply(entity);
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(ID id) {
        if (!storage.containsKey(id)) {
            throw new EntityNotFoundException("Entity", id);
        }
        storage.remove(id);
    }

    @Override
    public boolean exists(ID id) {
        return storage.containsKey(id);
    }

    @Override
    public long count() {
        return storage.size();
    }

}
