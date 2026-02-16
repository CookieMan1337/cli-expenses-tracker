package com.ledgerlite.io.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ledgerlite.report.ExportReportItem;
import com.ledgerlite.report.JsonExportItem;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class JsonReportExporter implements ReportExporter {
    private final ObjectMapper objectMapper;

    public JsonReportExporter() {
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    @Override
    public void export(List<ExportReportItem> data, Path path) {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            List<JsonExportItem> jsonItems = data.stream()
                    .map(item -> new JsonExportItem(
                            item.getPeriod(),
                            item.getCategory(),
                            item.getTotalAmount()))
                    .toList();
            String json = objectMapper.writeValueAsString(jsonItems);
            bw.write(json);
            log.trace("Запись в файл {} произведена", path);
        } catch (IOException exception) {
            log.error("Произошла ошибка во время сохранения файла  {}: {}", path, exception.getMessage());
            throw new RuntimeException("Ошибка сохранения JSON файла: " + path, exception);
        }
    }
}