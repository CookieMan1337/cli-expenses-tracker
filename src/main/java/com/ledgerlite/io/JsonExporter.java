package com.ledgerlite.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ledgerlite.report.PeriodSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonExporter {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void exportMonthlyReport(PeriodSummary summary) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "report_" + timestamp + ".json";
        Path filePath = Paths.get("exports", fileName);

        Files.createDirectories(filePath.getParent());

        mapper.writeValue(filePath.toFile(), summary);
        System.out.println("JSON файл сохранён: " + filePath.toAbsolutePath());
    }
}
