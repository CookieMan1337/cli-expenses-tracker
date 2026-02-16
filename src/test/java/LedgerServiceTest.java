
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.BudgetService;
import service.CategoryService;
import service.LedgerService;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class LedgerServiceTest {

    LedgerService ledgerService;
    CategoryService categoryService;
    BudgetService budgetService;

    @BeforeEach
    void setUp() throws Exception {
        categoryService = new CategoryService();
        budgetService = new BudgetService(categoryService);
        ledgerService=new LedgerService(categoryService, budgetService);
        categoryService.addCategory("f","food");
        categoryService.addCategory("t","transport");
    }


    @Test
    public void addIncome() {
        ledgerService.addIncome("2026-02-15","500","f","work");
        assertEquals(500.0,ledgerService.getIncRep().findAll().getLast().getAmount(),0.000);
        assertEquals("f",ledgerService.getIncRep().findAll().getLast().getCategoryCode());
    }

    @Test
    public void addExpense() {
        ledgerService.addIncome("2026-02-15","500","t","work");
        ledgerService.addExpense("2026-02-15","200","t","milk and oil");
        assertEquals(200.0,ledgerService.getExpRep().findAll().getLast().getAmount(),0.000);
        assertEquals("t",ledgerService.getExpRep().findAll().getLast().getCategoryCode());
    }
}