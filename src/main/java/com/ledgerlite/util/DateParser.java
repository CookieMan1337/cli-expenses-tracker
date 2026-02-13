package com.ledgerlite.util;

import com.ledgerlite.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {
    private DateParser(){}

    private static final DateTimeFormatter[] FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,   // YYYY-MM-DD
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    public static LocalDate parse(String dateStr){
        Validator.checkNotBlank(dateStr, "Дата");

        for (DateTimeFormatter formatter : FORMATS){
            try {
                return LocalDate.parse(dateStr.trim(),formatter);
            } catch (DateTimeParseException ignored){
                //Скипаем ошибки и пробуем другой формат
            }
        }
        //Иначе выдаем ошибку
        throw new ValidationException("Неправильный формат даты: " + dateStr);
    }

    public static LocalDate parseOrDefault(String dateStr, LocalDate defaultDate){
        if (dateStr == null || dateStr.trim().isEmpty()){
            return defaultDate;
        }
        return parse(dateStr);
    }

    public static LocalDate today(){
        return LocalDate.now();
    }
}
