package com.ledgerlite.persistence;

import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Transaction;
import com.ledgerlite.report.Report;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileStore {
    private static final Path TRANSACTIONS_PATH = Paths.get("data", "transactions.dat");
    private static final Path CATEGORIES_PATH = Paths.get("data", "categories.dat");

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        saveData(transactions, TRANSACTIONS_PATH);
    }
    public void saveCategories(List<Category> categories) throws IOException {
        saveData(categories, CATEGORIES_PATH);
    }


    @SuppressWarnings("unchecked")
    public List<Transaction> loadTransactions() throws IOException, ClassNotFoundException {
        return (List<Transaction>) loadData(TRANSACTIONS_PATH);
    }

    @SuppressWarnings("unchecked")
    public List<Category> loadCategories() throws IOException, ClassNotFoundException {
        return (List<Category>) loadData(CATEGORIES_PATH);
    }


    public void deleteTransaction(UUID id, List<Transaction> allTransactions) throws IOException {
        List<Transaction> updatedList = allTransactions.stream()
                .filter(t -> !t.getId().equals(id))
                .toList();
        saveTransactions(updatedList);
    }

    private void saveData(Object data, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Path temp = Files.createTempFile(path.getParent(), "temp", ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(data);
        }
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
    }

    private Object loadData(Path path) throws IOException, ClassNotFoundException {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
            return in.readObject();
        }
    }

}
