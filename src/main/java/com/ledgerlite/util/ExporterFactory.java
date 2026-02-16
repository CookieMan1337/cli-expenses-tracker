package com.ledgerlite.util;

import com.ledgerlite.io.exporter.CsvExporter;
import com.ledgerlite.io.exporter.JsonReportExporter;
import com.ledgerlite.io.exporter.ReportExporter;

public class ExporterFactory {
    public static ReportExporter getExporter(String format) {
        return switch (format) {
            case "csv" -> new CsvExporter();
            case "json" -> new JsonReportExporter();
            default -> throw new IllegalArgumentException("Неизвестный формат: " + format);
        };
    }
}