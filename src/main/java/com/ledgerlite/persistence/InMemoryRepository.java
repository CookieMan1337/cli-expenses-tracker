package com.ledgerlite.persistence;

import com.ledgerlite.domain.Transaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryRepository<T> implements Repository<T>{
    private final Map<UUID,T> store = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    //private final Function<T,K> keyExtractor;

    //    public InMemoryRepository(Function<T,K> keyExtractor){
//        this.keyExtractor = keyExtractor;
//    }
    private UUID extractId(T item){
        if (item instanceof Transaction t){
            return t.getId();
        } throw new IllegalArgumentException("Has no id");
    }

    @Override
    public Optional<T> findById(UUID id){
        lock.readLock().lock();
        try{
            return Optional.of(store.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }
    @Override
    public List<T> findAll(){
        lock.readLock().lock();
        try{
            return new ArrayList<>(store.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(T item){
        lock.writeLock().lock();
        try{
            UUID id = extractId(item);
            store.put(id,item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(UUID id){
        lock.writeLock().lock();
        try{
            store.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
