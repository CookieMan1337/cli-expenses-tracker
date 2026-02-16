package com.ledgerlite.persistence;

import java.util.List;
import java.util.Optional;

interface Repository<T> {
    T save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();

    boolean delete(String id);

    void deleteAll();
}
