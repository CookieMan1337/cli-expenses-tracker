package persistence;

import domain.Expense;

public class ExpenseRepository extends UnifiedRepository<Expense, String> {

    @Override
    public void save(Expense expense) {
        repository.put(expense.getId(), expense);
    }

    @Override
    public Expense findById(String id) {
        return repository.get(id);
    }
}
