package service;

import domain.Budget;
import exception.ValidationException;
import persistence.BudgetRepository;

import java.time.YearMonth;

public class BudgetService {
    private final CategoryService categoryService;
    private final BudgetRepository budgetRep = new BudgetRepository();

    public BudgetService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //установка значения бюджета
    public void setBudget(String periodIn, String categoryCode, String limitIn) {
        try {
            if (categoryService.checkCategoryExistence(categoryCode)) {
                String searchingId = categoryCode.toString() + "_" + periodIn.toString();
                YearMonth period = YearMonth.parse(periodIn);
                Double limit = Double.parseDouble(limitIn);
                Budget newBudget = new Budget(period, categoryCode, limit);

                if (budgetRep.existById(searchingId)) {
                    budgetRep.replace(searchingId, newBudget);
                    System.out.println("replace budget " + searchingId);
                } else {
                    budgetRep.save(newBudget);
                    System.out.println("save budget " + newBudget);
                }
                //budgetRep.findAll().forEach(System.out::println);
            } else {
                throw new ValidationException("Category not exists");
            }
        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
    }

    //добавление денег в бюджет
    public void addToBudget(YearMonth periodIn, String categoryCode, Double amount) {
        try {
            if (categoryService.checkCategoryExistence(categoryCode)) {
                String searchingId = categoryCode.toString() + "_" + periodIn.toString();

                if (budgetRep.existById(searchingId)) {
                    //заменяем, приплюсовывая значение
                    System.out.println("added money to this budget " + budgetRep.findById(searchingId));
                    Budget newBudget = new Budget(periodIn, categoryCode, amount + budgetRep.findById(searchingId).limit());
                    budgetRep.replace(searchingId, newBudget);
                    System.out.println("replace budget Plus added amount " + amount + " now:" + newBudget);
                } else {
                    //делаем новую, считая что было бюджет 0
                    Budget newBudget = new Budget(periodIn, categoryCode, amount);
                    budgetRep.save(newBudget);
                    System.out.println("save new budget " + newBudget);
                }
                //budgetRep.findAll().forEach(System.out::println);
            } else {
                throw new ValidationException("Category not exists");
            }
        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
    }

    //вычитание денег из бюджета
    public void subtractFromBudget(YearMonth periodIn, String categoryCode, Double amount) {
        try {
            if (categoryService.checkCategoryExistence(categoryCode)) {
                String searchingId = categoryCode.toString() + "_" + periodIn.toString();

                if (budgetRep.existById(searchingId)) {
                    //заменяем, вычитая значение
                    if (checkBudgetSufficiency(periodIn, categoryCode, amount)) {
                        System.out.println("subtract money from this budget " + budgetRep.findById(searchingId));
                        Budget newBudget = new Budget(periodIn, categoryCode, budgetRep.findById(searchingId).limit() - amount);
                        budgetRep.replace(searchingId, newBudget);
                        System.out.println("replace budget minus subtract amount " + amount + " now:" + newBudget);
                    } else {
                        throw new ValidationException("No enough budget");
                    }
                } else {
                    //нельзя уйти в минус, если бюджета нет на это сочетание
                    throw new ValidationException("No budget planned");
                }
                //budgetRep.findAll().forEach(System.out::println);
            } else {
                throw new ValidationException("Category not exists");
            }
        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }

    }

    //проверка на достаточность бюджета
    public boolean checkBudgetSufficiency(YearMonth periodIn, String categoryCode, Double amount) {
        try {
            if (categoryService.checkCategoryExistence(categoryCode)) {
                String searchingId = categoryCode.toString() + "_" + periodIn.toString();

                if (budgetRep.existById(searchingId)) {
                    //если существует, то чекаем достаточность средств
                    if (budgetRep.findById(searchingId).limit() - amount > 0) {
                        return true;
                    }
                } else {
                    //нет
                    throw new ValidationException("No budget planned");
                }
            } else {
                throw new ValidationException("Category not exists");
            }
        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
        return false;
    }
}
