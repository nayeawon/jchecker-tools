package edu.handong.csee.isel.tbar.utils;

import edu.handong.csee.isel.tbar.config.Configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class jCheckerUtils {
    public static String processRankingCsv(String path) {
        if (!path.endsWith("/")) path = path.concat("/");
        String targetFile = path + "ochiai.ranking.csv";
        File processedFile = new File(path + "intermediate/ranking.txt");
        processedFile.getParentFile().mkdirs();
        HashMap<String, String> suspiciousLines= new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(targetFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(processedFile));
            String line = reader.readLine();
            while((line = reader.readLine()) != null) {
                String packageName = line.substring(0, line.indexOf("$"));
                String fileName = line.substring(line.indexOf("$") + 1, line.indexOf("#"));
                String methodName = line.substring(line.indexOf("#") + 1, line.indexOf(":"));
                String lineNumber = line.substring(line.indexOf(":") + 1, line.indexOf(";"));
                String suspiciousValue = line.substring(line.indexOf(";") + 1);

                String fullPath = packageName + "." + fileName;
                if (!suspiciousValue.equals("0.0")) {
                    if (suspiciousLines.keySet().contains(fullPath)) {
                        suspiciousLines.get(fullPath).concat("," + lineNumber);
                    }
                    else suspiciousLines.put(fullPath, fullPath + "@" + lineNumber);
                }
            }

            for (String susLine : suspiciousLines.keySet()) {
                writer.write(suspiciousLines.get(susLine) + "\n");
            }

            writer.flush();
            writer.close();
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Configuration.knownBugPositions = processedFile.getAbsolutePath();
        return processedFile.getAbsolutePath();
    }

    public static void processFailedTests(String path) {
        if (!path.endsWith("/")) path = path.concat("/");
        String targetFile = path + "tests.csv";
        File processedFile = new File(path + "intermediate/tests.txt");
        processedFile.getParentFile().mkdirs();
        List<String> failedTests = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(targetFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(processedFile));
            String line = reader.readLine();
            int num = 0;
            while((line = reader.readLine()) != null) {
                String[] parsedLine = line.split(",");
                if (parsedLine[1].equals("FAIL")) {
                    String packageName = parsedLine[0].replace("#", "::");
                    failedTests.add(packageName);
                    num++;
                }
            }
            writer.write(num + "\n");
            for (String failedTest : failedTests) {
                writer.write(failedTest + "\n");
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
