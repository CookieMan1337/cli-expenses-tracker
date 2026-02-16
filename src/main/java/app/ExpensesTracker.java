package app;

import exception.NotEnoughWordsException;
import io.ExportFiles;
import io.ImportFiles;
import service.BudgetService;
import service.CategoryService;
import service.LedgerService;
import service.ReportService;

import java.util.Scanner;

public class ExpensesTracker {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        CategoryService categoryService = new CategoryService();
        BudgetService budgetService = new BudgetService(categoryService);
        LedgerService ledgerService = new LedgerService(categoryService, budgetService);
        ReportService reportService = new ReportService(ledgerService);
        ImportFiles importFiles = new ImportFiles();
        ExportFiles exportFiles = new ExportFiles();

        CommandParser commandParser = new CommandParser();

        while (true) {
            String command = sc.nextLine();
            CommandType commandType = commandParser.parseCommandType(command);

            String[] words = command.split("\\s+"); // parsing

            try {
                switch (commandType) {
                    case EXIT:
                        return;

                    case ADD_CAT: //add-cat CODE NAME
                        if (words.length == 3) {
                            categoryService.addCategory(words[1], words[2]);
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 2, words.length - 1, "CODE, NAME");
                        }

                    case ADD_INC: //add-inc YYYY-MM-DD AMOUNT CODE [NOTE...]
                        if (words.length == 5) {
                            ledgerService.addIncome(words[1], words[2], words[3], words[4]);
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 4, words.length - 1
                                    , "YYYY-MM-DD AMOUNT CODE [NOTE...]");
                        }

                    case ADD_EXP: //add-exp YYYY-MM-DD AMOUNT CODE [NOTE...]
                        if (words.length == 5) {
                            ledgerService.addExpense(words[1], words[2], words[3], words[4]);
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 4, words.length - 1
                                    , "YYYY-MM-DD AMOUNT CODE [NOTE...]");
                        }

                    case SET_BUDGET: //set-budget YYYY-MM CODE LIMIT
                        if (words.length == 4) {
                            budgetService.setBudget(words[1], words[2], words[3]);
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 3, words.length - 1, "YYYY-MM CODE LIMIT");
                        }

                    case REP_MONTH: //report month YYYY-MM
                        if (words.length == 3) {
                            reportService.getReportByMonth(words[2]);
                            System.out.println(reportService.getReportByMonth(words[2]).toString());
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 1, words.length - 1, "YYYY-MM");
                        }

                    case REP_TOP: //report top N
                        if (words.length == 3) {
                            reportService.getTopReport(words[2]);
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 1, words.length - 1, "N - number");
                        }

                    case IMPORT_CSV: //import csv path/to/file.csv
                        if (words.length == 3) {
                            importFiles.ImportCsvFile(words[2]);
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 2, words.length - 1, "path/to/file.csv");
                        }

                    case EXPORT_REP: //export report json|csv path/to/out
                        if (words.length == 4) {
                            //export report csv C:\Users\Marinka\IdeaProjects\cli-expenses-tracker\11111.csv
                            if (words[2].trim().equals("csv")) {
                                exportFiles.ExportFullReportToCsv(words[3]);
                            } else if (words[2].trim().equals("json")) {
                                exportFiles.ExportFullReportToJson(words[3]);
                            } else {
                                System.out.println("Unsupported type of file");
                            }
                            break;
                        } else {
                            throw new NotEnoughWordsException(commandType, 2, words.length - 1, "json|csv path/to/out");
                        }

                    case UNDO: //set-budget YYYY-MM CODE LIMIT
                        ledgerService.undo();
                        break;

                    case UNKNOWN: //set-budget YYYY-MM CODE LIMIT
                        System.out.println("Unknown command");
                        break;
                }
            } catch (NotEnoughWordsException e) {
                System.out.println(e);
            }

        }
    }

}
