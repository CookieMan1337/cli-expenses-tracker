package com.ledgerlite.io.importer;

import lombok.Data;

@Data
public class ImportResult {
    private int totalRows;
    private int successful;
    private int failed;

    public ImportResult() {
        this.totalRows = 0;
        this.successful = 0;
        this.failed = 0;
    }

    public void incrementTotalRows() {
        totalRows++;
    }

    public void incrementSuccessful() {
        successful++;
    }

    public void incrementFailed() {
        failed++;
    }
}