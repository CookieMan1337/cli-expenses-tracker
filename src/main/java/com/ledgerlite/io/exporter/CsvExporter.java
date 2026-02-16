package com.ledgerlite.io.exporter;

import com.ledgerlite.report.ExportReportItem;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class CsvExporter implements ReportExporter {

    @Override
    public void export(List<ExportReportItem> data, Path path) {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            bw.write("period,category,total");
            bw.newLine();
            for (ExportReportItem item : data) {
                String reportString = toString(item);
                bw.write(reportString);
                bw.newLine();
            }
            log.trace("Запись в файл {} произведена", path);
        } catch (IOException exception) {
            log.error("Произошла ошибка во время сохранения файла  {}: {}", path, exception.getMessage());
            throw new RuntimeException("Произошла ошибка во время сохранения файла  " + path + ": "
                    + exception.getMessage());
        }
    }

    private String toString(ExportReportItem item) {
        if (item == null) {
            return "";
        }
        return String.format("%s,%s,%s",
                item.getPeriod(), item.getCategory(), item.getTotalAmount());
    }
}
