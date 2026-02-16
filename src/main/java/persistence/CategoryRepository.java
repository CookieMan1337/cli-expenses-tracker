package persistence;

import domain.Category;

public class CategoryRepository extends UnifiedRepository<Category, String> {

    @Override
    public void save(Category category) {
        repository.put(category.code(), category);
    }
}
