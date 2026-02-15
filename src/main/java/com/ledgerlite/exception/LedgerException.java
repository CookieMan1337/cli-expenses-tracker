package com.ledgerlite.exception;

/**
 * Базовое исключение, наследуемся от RuntimeException
 */
public class LedgerException extends RuntimeException {


    public LedgerException(String message) {
        super(message);
    }
    // один и тот же метод с перегрузкой
    // на самом деле перегрузка - недооцененная имба!!!
    public LedgerException(String message, Throwable cause) {
        // конструктор род. класса
        super(message, cause);
    }
}
