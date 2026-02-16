package persistence;

import domain.Income;

public class IncomeRepository extends UnifiedRepository<Income, String> {

    @Override
    public void save(Income income) {
        repository.put(income.getId(), income);
    }
}
