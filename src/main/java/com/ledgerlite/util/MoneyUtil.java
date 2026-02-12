package com.ledgerlite.util;

import com.ledgerlite.domain.Money;

public final class MoneyUtil {
    private MoneyUtil(){}
    public static Money parseMoney(String s){
        return Money.parse(s);
    }
}
