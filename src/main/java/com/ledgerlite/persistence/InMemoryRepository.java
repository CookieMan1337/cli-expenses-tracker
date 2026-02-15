package com.ledgerlite.persistence;

import java.util.*;
import java.util.function.Function;

/**
 * Реализация хранилища "в памяти". Данные пропадут при выключении программы
 * Дженерики - «Я пока не знаю, что это за типы, но я обещаю,
 * что везде, где написано T, будет один и тот же тип объекта».
 * Map<ID, T> — где ID тип ключа (UUID),
 * а T — что угодно (Транзакция).
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    // Map<ID, T> (final  потому что запись историческая и ее менять нельзя)
    private final Map<ID, T> storage = new HashMap<>();

    // функция, которая "вытащит" ID
    private final Function<T, ID> idExtractor;

    /**
     * Конструктор репозитория.
     * @param idExtractor — правило, как достать ID из объекта.
     * Передаем в конструктор функцию, которую при вызове уточним
     * Для транзакций getId
     * new InMemoryRepository<Transaction, UUID>(t -> t.getId());
     */
    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }


    // Переопределяем методы из интерфейса

    @Override
    public void save(T item) {
        // Достаем ID из объекта и кладем объект (item) в "коробочку" storage (наша HashMap)
        ID id = idExtractor.apply(item);
        storage.put(id, item); // ключ - айди транзакции, значение - наш обджект транзакции
    }

    @Override
    public Optional<T> findById(ID id) {
        // ищем по айди которое указываем при обращении через интерфейс
        // ofNullable вернет пустой Optional, если в Map ничего не нашлось
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Collection<T> findAll() {
        // Возвращаем все значения из мапы
        return storage.values();
    }

    @Override
    public void delete(ID id) {
        storage.remove(id);
    }
}