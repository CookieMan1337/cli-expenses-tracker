package com.ledgerlite.service;

import com.ledgerlite.domain.*;
import com.ledgerlite.exception.ValidationException;
import com.ledgerlite.persistence.InMemoryRepository;
import com.ledgerlite.persistence.Repository;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LedgerService {
    private final Repository<Transaction, UUID> transactionRepository;
    private final Repository<Category, String> categoryRepository;

    public LedgerService(
            Repository<Transaction, UUID> transactionRepository,
            Repository<Category, String> categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        initializeDefaultCategories();
    }

    public LedgerService() {
        // Для Transaction: ID = UUID (Transaction.getId())
        this.transactionRepository = new InMemoryRepository<>(
                Transaction::getId
        );

        // Для Category: ID = String (код категории)
        this.categoryRepository = new InMemoryRepository<>(
                Category::code
        );

        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        for (Category category : Category.defaultCategories()){
            categoryRepository.save(category);
        }
    }

    // Транзакции

    public Optional<Transaction> getTransaction(UUID id) {  // Принимаем UUID
        return transactionRepository.findById(id);  // Передаем UUID (Object)
    }

    public Optional<Transaction> getTransaction(String idString) {  // Перегрузка для String
        try {
            UUID id = UUID.fromString(idString);
            return getTransaction(id);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid transaction ID format: " + idString);
        }
    }

    public void removeTransaction(UUID id) {  // Принимаем UUID
        transactionRepository.delete(id);  // Передаем UUID (Object)
    }

    public void removeTransaction(String idString) {  // Перегрузка для String
        try {
            UUID id = UUID.fromString(idString);
            removeTransaction(id);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid transaction ID format: " + idString);
        }
    }

    public Income addIncome(LocalDate date, Money amount, Category category, String note) {
        validateTransactionParams(date, amount, category);
        Income income = new Income(date, amount, category, note);
        transactionRepository.save(income);
        return income;
    }

    public Expense addExpense(LocalDate date, Money amount, Category category, String note) {
        validateTransactionParams(date, amount, category);
        Expense expense = new Expense(date, amount, category, note);
        transactionRepository.save(expense);
        return expense;
    }

    private void validateTransactionParams(LocalDate date, Money amount, Category category) {
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Transaction date cannot be in the future");
        }
        if (!categoryRepository.exists(category.code())) {
            throw new ValidationException("Category not found: " + category.name());
        }
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    //Категории

    public Category addCategory(String code,String name){
        if (categoryRepository.exists(code.toUpperCase())){
            throw new ValidationException("Категория " + name + " уже существует.");
        }

        Category category = new Category(code, name);
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategory(String code){
        return categoryRepository.findById(code);
    }

    public void removeCategory(String code){
        boolean hasTransactions = transactionRepository.findAll().stream()
                .anyMatch(t -> t.getCategory().code().equals(code));

        if (hasTransactions) {
            throw new ValidationException("Нельзя удалить категорию у которой есть транзакции.");
        }

        categoryRepository.delete(code);
    }

    //Сводка

    public Money getBalance() {
        Money totalIncome = getTotalIncome();
        Money totalExpense = getTotalExpense();
        return totalIncome.subtract(totalExpense);
    }

    public Money getTotalIncome(){
        return transactionRepository.findAll().stream()
                .filter(t -> t instanceof Income)
                .map(Transaction::getAmount)
                .reduce(Money.zero(Currency.getInstance("RUB")),Money::add);
    }

    public Money getTotalExpense(){
        return transactionRepository.findAll().stream()
                .filter(t -> t instanceof Expense)
                .map(Transaction::getAmount)
                .reduce(Money.zero(Currency.getInstance("RUB")),Money::add);
    }

}
