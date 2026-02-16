package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class UnifiedRepository<T, ID> implements Repository<T, ID> {
    protected Map<ID, T> repository = new HashMap<>();

    @Override
    public List<T> findAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public void save(T t) {
    }

    @Override
    public void deleteById(ID id) {
        repository.remove(id);
    }

    @Override
    public T findById(ID id) {
        return repository.get(id);
    }

    @Override
    public boolean existById(ID id) {
        return repository.containsKey(id);
    }
}
