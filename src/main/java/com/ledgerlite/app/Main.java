package com.ledgerlite.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

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
