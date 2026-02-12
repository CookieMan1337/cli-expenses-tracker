package com.ledgerlite.app;

public enum Command {
    HELP("help","Список команд"),
    ADD_INCOME("ADD_INCOME", "Добавить новый доход"),
    ADD_EXPENSE("ADD_EXPENSE", "Добавить новую строку расхода"),
    LIST("LIST", "Список транзакций"),
    BALANCE("BALANCE", "Текущий баланс"),
    ADD_CATEGORY("ADD_CATEGORY", "Добавить категорию трат"),
    LIST_CATEGORY("LIST_CATEGORY", "Доступные категории"),
    REMOVE("REMOVE", "Удалить транзакцию"),
    REPORT_MONTH("REPORT_MONTH", "Показать отчёт за текущий месяц"),
    REPORT_TOP("REPORT_TOP", "Показать топ-10 расходов"),
    EXIT("EXIT", "Завершить работу"),
    UNKNOWN("","");

    private final String text;
    private final String description;

    Command(String text, String description){
        this.text = text;
        this.description = description;
    }

    public static Command fromString(String text) {
        for (Command cmd : values()){
            if (cmd.text.equalsIgnoreCase(text.trim())){
                return cmd;
            }
        }
        return UNKNOWN;
    }
    public static String getHelp(){
        StringBuilder sb = new StringBuilder("Доступные команды:\n");
        for (Command cmd : values()){
            if (cmd != UNKNOWN){
                sb.append(String.format(" %-20s %s\n", cmd.text, cmd.description));
            }
        }
        return sb.toString();
    }
}
