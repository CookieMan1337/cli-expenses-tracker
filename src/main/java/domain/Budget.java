package domain;

import java.time.YearMonth;

public record Budget(YearMonth period, String category, Double limit) {

}
