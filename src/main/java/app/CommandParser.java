package app;

import exception.NotEnoughWordsException;
import io.ExportFiles;
import io.ImportFiles;
import service.BudgetService;
import service.CategoryService;
import service.LedgerService;
import service.ReportService;

public class CommandParser {

    public CommandType parseCommandType(String command) {
        if (command.equals("exit")) {
            return CommandType.EXIT;
        } else if (command.startsWith("add-cat")) {
            return CommandType.ADD_CAT;
        } else if (command.startsWith("add-inc")) {
            return CommandType.ADD_INC;
        } else if (command.startsWith("add-exp")) {
            return CommandType.ADD_EXP;
        } else if (command.startsWith("set-budget")) {
            return CommandType.SET_BUDGET;
        } else if (command.startsWith("report month")) {
            return CommandType.REP_MONTH;
        } else if (command.startsWith("report top")) {
            return CommandType.REP_TOP;
        } else if (command.startsWith("import csv")) {
            return CommandType.IMPORT_CSV;
        } else if (command.startsWith("export report")) {
            return CommandType.EXPORT_REP;
        } else if (command.equals("undo")) {
            return CommandType.UNDO;
        } else {
            return CommandType.UNKNOWN;
        }
    }
}
