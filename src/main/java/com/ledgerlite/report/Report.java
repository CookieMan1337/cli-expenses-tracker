package com.ledgerlite.report;

import com.ledgerlite.domain.Category;
import com.ledgerlite.domain.Money;

import java.time.YearMonth;
import java.util.Map;

public record Report(YearMonth period, Money totalIncome, Money totalExpense, Map<Category,Money> expensesInCategories) {


    @Override
    public String toString(){
        return "Report{ " + "period = " + period + ", totalIncome = " + totalIncome + ", TotalExpense =" + totalExpense + ", expensesInCategories=" + expensesInCategories.entrySet() + " }";
    }
}
