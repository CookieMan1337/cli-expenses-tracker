package com.ledgerlite.app;

import com.ledgerlite.domain.Money;
import com.ledgerlite.exception.LedgerException;
import com.ledgerlite.service.LedgerService;
import com.ledgerlite.util.DateUtil;
import com.ledgerlite.util.MoneyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
//    static Repository<Category, String> categoryRepository = new InMemoryRepository<Category, String>(Category::code);
//    static Repository<Transaction, UUID> transactionRepository = new InMemoryRepository<Transaction, UUID>(Transaction::getId);
//    static LedgerService ledger = new LedgerService(categoryRepository, transactionRepository);

    public static void main(String[] args) {
        log.info("App starting...");
        App app = new App();

        try (BufferedReader breader = new BufferedReader(new InputStreamReader(System.in))) {
            String s;
            while (true) {
                System.out.println("write command: ");
                s = breader.readLine();
                if (s == null) {
                    break;
                }
                s = s.trim();
                if (s.toLowerCase().equals("exit")) {
                    log.info("exit");
                    break;
                }
                app.handleCommand(s);


            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            log.error("CLI error ", ex);
        }
    }

}
