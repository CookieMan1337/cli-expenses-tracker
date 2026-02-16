package persistence;

import java.util.List;

public interface Repository<T, ID> {
    public void save(T t);

    public void deleteById(ID id);

    public List<T> findAll();

    public T findById(ID id);

    public boolean existById(ID id);
}
