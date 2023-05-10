package edu.handong.csee.isel.tbar.utils;

import java.io.*;

public class FileUtils {
    public static int getGZoltarResultFromFile(String fileAddress) {
        BufferedReader reader = null;
        int errorNum = 0;
        try {
            reader = new BufferedReader(new FileReader(fileAddress));
            String firstLine = reader.readLine();
            if (firstLine.startsWith("Failing tests:")){
                errorNum =  Integer.valueOf(firstLine.split(":")[1].trim());
            }
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
        return errorNum;
    }
}
