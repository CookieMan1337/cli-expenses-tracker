package main.java.com.ledgerlite.persistence;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entityy);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(ID id);
    boolean exists(ID id);
    long count();
}
