package persistence;

import domain.Budget;

public class BudgetRepository extends UnifiedRepository<Budget, String> {

    @Override
    public void save(Budget budget) {
        String id = budget.category().toString() + "_" + budget.period().toString();
        repository.put(id, budget);
    }

    public void replace(String idToReplace, Budget newBudget) {
        repository.replace(idToReplace, newBudget);
    }
}
