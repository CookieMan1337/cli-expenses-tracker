package service;

import domain.Expense;
import domain.Income;
import exception.ValidationException;
import lombok.Getter;
import persistence.ExpenseRepository;
import persistence.IncomeRepository;

import java.time.LocalDate;
import java.time.YearMonth;

public class LedgerService {

    private final ExpenseRepository expRep = new ExpenseRepository();
    private final IncomeRepository incRep = new IncomeRepository();
    private final BudgetService budgetService;
    private final CategoryService categoryService;

    public LedgerService(CategoryService categoryService, BudgetService budgetService) {
        this.budgetService = budgetService;
        this.categoryService = categoryService;
    }

    //добавление дохода
    public void addIncome(String dateIn, String amountIn, String categoryCode, String note) {
        LocalDate date = LocalDate.parse(dateIn);
        Double amount = Double.parseDouble(amountIn);

        try {
            if (categoryService.checkCategoryExistence(categoryCode)) {
                Income inc = new Income(date, amount, categoryCode, note);
                //save income
                incRep.save(inc);
                System.out.println("Income added");
                incRep.findAll().forEach(System.out::println);
                budgetService.addToBudget(YearMonth.from(date), categoryCode, amount);
            } else {
                throw new ValidationException("Category not exists");
            }

        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
    }

    //добавление расхода
    public void addExpense(String dateIn, String amountIn, String categoryCode, String note) {

        LocalDate date = LocalDate.parse(dateIn);
        Double amount = Double.parseDouble(amountIn);

        try {
            if (categoryService.checkCategoryExistence(categoryCode)) {
                if (budgetService.checkBudgetSufficiency(YearMonth.from(date), categoryCode, amount)) {
                    Expense exp = new Expense(date, amount, categoryCode, note);
                    expRep.save(exp);
                    System.out.println("Expense added");
                    expRep.findAll().forEach(System.out::println);
                    budgetService.subtractFromBudget(YearMonth.from(date), categoryCode, amount);
                } else {
                    throw new ValidationException("Not enough budget");
                }

            } else {
                throw new ValidationException("Category not exists");
            }
        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
    }

    //отмена действия
    public void undo() {
        /*
        TBD
        */
    }

    //получение ссылки на репозиторий расхода
    public ExpenseRepository getExpRep() {
        return expRep;
    }

    //получение ссылки на репозиторий доходов
    public IncomeRepository getIncRep() {
        return incRep;
    }
}
