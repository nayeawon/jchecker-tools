package edu.handong.csee.isel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class GZoltarRunner {
    private static final String shellPath = "/home/DPMiner/lib/FL.sh";
    public String run(String srcPath, String testPath) {
        ArrayList<String> command = new ArrayList<>();
        command.add(shellPath);
        command.add(srcPath);
        command.add(testPath);

        ProcessBuilder builder;
        Process process = null;

        try {
            builder = new ProcessBuilder(command);
            builder.directory(new File(srcPath));

            process = builder.start();

            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            process.destroy();
        }

        ArrayList<Data> suspiciousResult = new ArrayList<>();
        parseResult(srcPath, suspiciousResult);
        String sheet = convertToString(suspiciousResult);
        return sheet;
    }

    private void parseResult(String srcPath, ArrayList<Data> suspiciousResult) {
        String sflResultPath = srcPath + "/sfl/txt/ochiai.ranking.csv";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sflResultPath));
            // line
            // edu.handong.csee.java.hw2$MathDriver#run(java.lang.String[]):118;1.0
            int count = 0;
            String line = reader.readLine();
            while (count < 3) {
                line = reader.readLine();
                // priority rules
                String packageName = line.substring(0, line.indexOf("$"));
                String fileName = line.substring(line.indexOf("$") + 1, line.indexOf("#"));
                String methodName = line.substring(line.indexOf("#") + 1, line.indexOf(":"));
                int lineNumber = Integer.parseInt(line.substring(line.indexOf(":") + 1, line.indexOf(";")));
                String suspiciousValue = line.substring(line.indexOf(";") + 1);

                String fileContents;
                try {
                    byte[] bytes = Files.readAllBytes(new File(srcPath + "/src/" + packageName.replace(".", "/") + "/" + fileName + ".java").toPath());
                    fileContents = new String(bytes, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (fileContents.contains("public static void main")) continue;
                String targetString;
                try (Stream<String> lines = Files.lines(Paths.get(srcPath + "/src/" + packageName.replace(".", "/") + "/" + fileName + ".java"))) {
                    targetString = lines.skip(lineNumber - 1).findFirst().get();
                }

                if (Float.parseFloat(suspiciousValue) == 0.0) {
                    targetString = "";
                    lineNumber = 0;
                    fileName = "";
                }
                suspiciousResult.add(new Data(targetString, Float.parseFloat(suspiciousValue), lineNumber, fileName));
                count++;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToString(ArrayList<Data> suspiciousResult) {
        JsonObject score = new JsonObject();
        String[] index = new String[]{"first", "second", "third"};
        int idx = 0;
        for (Data data : suspiciousResult) {
            JsonObject info = new JsonObject();
            info.addProperty("line", data.getLine());
            info.addProperty("suspicious", data.getSuspiciousValue());
            info.addProperty("lineNum", data.getLineNumber());
            info.addProperty("file", data.getFileName());
            score.add(index[idx], info);
            idx++;
        }
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        String sheet = gson.toJson(score);
        return sheet;
    }
}
