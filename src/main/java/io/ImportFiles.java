package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
Пока выводится на экран, позже будет возвращать в return полученные данные
 */
public class ImportFiles {

    public void ImportCsvFile(String fileName) {
        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                for (String item : row) {
                    System.out.printf("%-10s", item);

                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while reading file " + fileName + ": " + e.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error while closing file " + e.getMessage());
                //throw new RuntimeException(e);
            }
        }

    }
}
