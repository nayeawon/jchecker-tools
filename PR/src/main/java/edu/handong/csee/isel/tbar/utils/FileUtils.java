package edu.handong.csee.isel.tbar.utils;

import edu.handong.csee.isel.tbar.config.Configuration;

import java.io.*;
import java.util.List;

public class FileUtils {
    public static int getGZoltarResultFromFile(String fileAddress, List<String> failedTests) {
        BufferedReader reader = null;
        int errorNum = 0;
        try {
            reader = new BufferedReader(new FileReader(fileAddress));
            String line = reader.readLine();
            errorNum =  Integer.valueOf(line.trim());
            while((line = reader.readLine()) != null) {
                failedTests.add(line);
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

    public static String getFileAddressOfJava(String srcPath, String className) {
        if (className.contains("<") && className.contains(">")) {
            className = className.substring(0, className.indexOf("<"));
        }
        return srcPath.trim() + System.getProperty("file.separator")
                + className.trim().replace('.', System.getProperty("file.separator").charAt(0)) + ".java";
    }

    public static String getFileAddressOfClass(String classPath, String className) {
        if (className.contains("<") && className.contains(">")) {
            className = className.substring(0, className.indexOf("<"));
        }
        return classPath.trim() + System.getProperty("file.separator")
                + className.trim().replace('.', System.getProperty("file.separator").charAt(0)) + ".class";
    }

    public static String tempJavaPath(String classname, String identifier) {
        new File(Configuration.TEMP_FILES_PATH + identifier).mkdirs();
        return Configuration.TEMP_FILES_PATH + identifier + "/" + classname.substring(classname.lastIndexOf(".") + 1) + ".java";
    }

    public static String tempClassPath(String classname, String identifier) {
        new File(Configuration.TEMP_FILES_PATH + identifier).mkdirs();
        return Configuration.TEMP_FILES_PATH + identifier + "/" + classname.substring(classname.lastIndexOf(".") + 1) + ".class";
    }
}
