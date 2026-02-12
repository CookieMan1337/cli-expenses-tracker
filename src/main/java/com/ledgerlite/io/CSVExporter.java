package com.ledgerlite.io;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter{


    public CSVExporter(){
    }

    public static <T> void export(List<T> objects, String output) throws Exception  {
        try (FileWriter writer = new FileWriter(output)) {
            var builder = new StatefulBeanToCsvBuilder<T>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withEscapechar(CSVWriter.NO_ESCAPE_CHARACTER)
                    .withLineEnd(CSVWriter.DEFAULT_LINE_END)
                    .build();

            builder.write(objects);
        } catch (IOException e){
            throw new IllegalArgumentException("Cannot export CSV, file path: " + output, e);
        }
    }


    public static <T> void export(T object, String output) throws IOException {
        try (FileWriter writer = new FileWriter(output)) {
            var builder = new StatefulBeanToCsvBuilder<T>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withEscapechar(CSVWriter.NO_ESCAPE_CHARACTER)
                    .withLineEnd(CSVWriter.DEFAULT_LINE_END)
                    .build();

            builder.write(object);
        } catch (IOException e){
            throw new IllegalArgumentException("Cannot export CSV, file path: " + output, e);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        }
    }

}
