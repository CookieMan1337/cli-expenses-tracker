package com.ledgerlite.report;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
public class ExportReportItem {
    private YearMonth period;
    private String category;
    private BigDecimal totalAmount;

    public ExportReportItem(YearMonth period, String category, BigDecimal totalAmount) {
        this.period = period;
        this.category = category;
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "ExportReportItem{" +
                "period=" + period +
                ", category='" + category + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
