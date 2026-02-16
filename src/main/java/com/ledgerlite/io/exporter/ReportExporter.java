package com.ledgerlite.io.exporter;

import com.ledgerlite.report.ExportReportItem;

import java.nio.file.Path;
import java.util.List;

public interface ReportExporter {
    void export(List<ExportReportItem> data, Path path);
}
