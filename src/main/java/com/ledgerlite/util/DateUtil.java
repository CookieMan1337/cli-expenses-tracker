package com.ledgerlite.util;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtil {
    private DateUtil(){}

    public static LocalDate parseDate(String s){
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
    }
    public static LocalDate parsePeriod(String s){
        return LocalDate.parse(s+"-01", DateTimeFormatter.ISO_LOCAL_DATE);
    }

}
