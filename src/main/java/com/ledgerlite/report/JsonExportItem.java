package com.ledgerlite.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
public class JsonExportItem {
    @JsonProperty("period")
    private String period;

    @JsonProperty("category")
    private String category;

    @JsonProperty("total")
    private BigDecimal total;


    public JsonExportItem(YearMonth period, String category, BigDecimal total) {
        this.period = period.toString();
        this.category = category;
        this.total = total;
    }
}