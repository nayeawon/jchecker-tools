package edu.handong.csee.isel.tbar.utils;

import java.io.*;

public class FileUtils {
    public static int getGZoltarResultFromFile(String fileAddress) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileAddress));
            String firstLine = reader.readLine();
            System.out.println(firstLine);
            if (firstLine.length() == 1 && Character.isDigit(firstLine.charAt(0))) return firstLine.charAt(0);
            else return -1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {}
            }
        }
    }
}
