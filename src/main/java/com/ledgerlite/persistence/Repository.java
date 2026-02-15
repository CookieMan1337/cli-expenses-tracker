package com.ledgerlite.persistence;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс для хранения объектов в памяти
 * @param <T>  — это тип объекта, который мы храним (Transaction)
 * @param <ID>  — это тип ключа, по которому мы ищем (UUID)
 */

public interface Repository<T, ID> {

    // Сохранить объект
    void save(T item);

    // Найти объект по его идентификатору
    Optional<T> findById(ID id);

    // Получить все объекты в виде коллекции
    Collection<T> findAll();

    // Удалить объект по ID
    void delete(ID id);
}