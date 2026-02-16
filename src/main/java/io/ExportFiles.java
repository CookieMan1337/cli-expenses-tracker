package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/*
Пока временно строка хардкодится тут, позже будет получать на вход структурки с данными + переведено в сериализацию
 */

public class ExportFiles {
    public void ExportFullReportToCsv(String filePathToBePlaced) {
        String filePath = filePathToBePlaced + "FullReport.csv";
        System.out.println(filePath);
        BufferedWriter bw = null;
        File file = new File(filePath);
        try {
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            String line = "test,2,tes";
            bw.write(line);
            System.out.println(line);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while reading file " + e.getMessage());
        } finally {
            try {
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while closing file " + e.getMessage());
                //throw new RuntimeException(e);
            }
        }
    }

    public void ExportFullReportToJson(String filePath) {

    }
}
