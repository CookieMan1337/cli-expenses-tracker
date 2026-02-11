package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.ValidationException;
import com.ledgerlite.persistence.FileStore;
import com.ledgerlite.persistence.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


public class LedgerService {
    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);
    // private final Repository<Category,String> categoryRepository;
    private final Repository<Transaction> transactionRepository;
    //private final Repository<Category> categoryRepository;
    //private final Repository<Budget> budgetRepository;
    private final Map<YearMonth,Map<Category,Budget>> budgets ;//= new ConcurrentHashMap<>();
    private final Map<String,Category> catCache = new ConcurrentHashMap<>();
    private final FileStore fileStore;

    private final Deque<String> undoStack = new ArrayDeque<>();
    private final int maxUndoSteps = 10;



    public LedgerService(Repository<Transaction> transactionRepository, Map<YearMonth,Map<Category,Budget>> budgets, FileStore fileStore){

        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.budgets = Objects.requireNonNull(budgets);
        this.fileStore = fileStore;
        loadFromFile(); // Загружаем данные при старте
    }

    private void loadFromFile() {
        try {
            List<Transaction> savedTransactions = fileStore.loadTransactions();
            savedTransactions.forEach(transactionRepository::save);
            List<Category> savedCategories = fileStore.loadCategories();
            savedCategories.forEach(cat -> catCache.put(cat.code(),cat));
        } catch (Exception e) {
            System.err.println("loadFromFile error: " + e.getMessage());
        }
    }
    private void saveToFile() {
        try {
            fileStore.saveTransactions(new ArrayList<>(transactionRepository.findAll()));
            fileStore.saveCategories(new ArrayList<>(getAllCategories()));
        } catch (IOException e) {
            throw new RuntimeException("saveToFile error:", e);
        }
    }
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("Nothing to undo");
        }

        String lastAction = undoStack.pop();

        try {
            UUID transactionId = UUID.fromString(lastAction);
            undoAddTransaction(transactionId);
            log.info("Undo performed: {}", lastAction);
        } catch (Exception e) {
            undoStack.push(lastAction);
            throw new RuntimeException("Failed to undo operation: " + e.getMessage(), e);
        }
    }

    private void undoAddTransaction(UUID transactionId) {
        transactionRepository.delete(transactionId);
        try {
            List<Transaction> allTransactions = getAllTransactions();
            fileStore.deleteTransaction(transactionId, allTransactions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update file during undo", e);
        }
        log.info("Undo ADD: removed transaction {}", transactionId);
    }

    private void pushToUndoStack(String action) {
        if (undoStack.size() >= maxUndoSteps) {
            undoStack.removeLast();
        }
        undoStack.push(action);
    }

    public void addCategory(String code, String name) {
        if (!catCache.containsKey(code)) {
            Category cat = new Category(code, name);
            log.info("Category added : " + code + " " + name);
            catCache.put(code, cat);
            saveToFile();
        } else {
            throw new ValidationException("Category already exists");
        }
    }

    public Category getCategoryByCode(String code) {
        if (code == null) {
            return null;
        }
        Category cat = catCache.get(code);
        if (cat == null){
            throw new ValidationException("Category not found");
        }
        return cat;
    }

    private Collection<Category> getAllCategories(){
        return new ArrayList<>(catCache.values());
    }

    public void addIncome(LocalDate date, Money amount, String code, String note){
        addIncome(date, amount, getCategoryByCode(code),note);
    }
    public void addIncome(LocalDate date, Money amount, Category cat, String note) {
        Income income = new Income(date, amount, cat, note);
        transactionRepository.save(income);
        saveToFile();
        pushToUndoStack(income.getId().toString());
        log.info("Income added : " + date.toString() + " " + amount.toString());
        // autosave();
    }

    public void addExpense(LocalDate date, Money amount, String code, String note){
        addExpense(date, amount, getCategoryByCode(code),note);
    }
    public void addExpense(LocalDate date, Money amount, Category cat, String note) {
        Expense expense = new Expense(date, amount, cat, note);
        transactionRepository.save(expense);
        saveToFile();
        pushToUndoStack(expense.getId().toString());
        log.info("Expense added : " + date.toString() + " " + amount.toString());
        // autosave();
    }

    public void setBudget(YearMonth period, String code, Money limit){
        Category cat = getCategoryByCode(code);
        Budget budget = new Budget(period,cat,limit);
        Map<Category,Budget> categoryBudgetMap = new ConcurrentHashMap<>();
        categoryBudgetMap.put(cat,budget);
        budgets.put(period,categoryBudgetMap);
        System.out.println(budgets.entrySet().stream().toList());
        log.info("Budget setted : " + period.toString() + " " + cat.toString() + " " + limit.toString());
    }

    private Money getTotalExpensesByPeriodAndCategory(YearMonth period, Category category){
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        return transactionRepository.findAll().stream()
                .filter(t -> t instanceof Expense)
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .filter(t -> t.getCategory().equals(category))
                .map(Transaction::getAmount)
                .reduce(new Money(BigDecimal.ZERO, Currency.getInstance("RUB")), Money::add);
    }


    public List<Transaction> getAllTransactions(){
        return new ArrayList<>(transactionRepository.findAll());
    }

    public List<Income> getIncomesByPeriod(YearMonth period){
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        return getAllTransactions().stream()
                .filter(t -> t instanceof Income)
                .map(t -> (Income) t)
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .toList();
    }

    public List<Expense> getExpensesByPeriod(YearMonth period){
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        return getAllTransactions().stream()
                .filter(t -> t instanceof Expense)
                .map(t -> (Expense) t)
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .toList();
    }

    public Map<Category,Money> getExpensesInCategories(YearMonth period){
        return getExpensesByPeriod(period).stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.reducing(new Money(BigDecimal.ZERO, Currency.getInstance("RUB")), Expense::getAmount, Money::add)));
    }
}
