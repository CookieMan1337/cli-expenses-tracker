package com.ledgerlite.persistence;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {
    Optional<T> findById(UUID id);
    List<T> findAll();
    void save(T item);
    void delete(UUID id);
}
