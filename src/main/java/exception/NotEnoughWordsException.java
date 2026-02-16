package exception;

import app.CommandType;

public class NotEnoughWordsException extends Exception {

    public NotEnoughWordsException(String message) {
        super(message);
    }

    public NotEnoughWordsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughWordsException(CommandType command, int argsExpected, int argsGet, String descr) {
        super("Command " + command + " was expecting " + argsExpected + " arguments (" + descr + "), but got " + argsGet);
    }
}
