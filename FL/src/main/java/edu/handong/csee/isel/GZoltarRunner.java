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

/**
 * Execute GZoltar, and parse result into JSON format.
 * @author nayeawon
 */
public class GZoltarRunner {
    private static final String shellPath = "/home/DPMiner/lib/FL.sh";

    /**
     * Use ProcessBuilder to execute GZoltar.
     * Use Gson by com.google.gson to parse the result of GZoltar into JSON format
     * @param srcPath path to source classes
     * @param testPath path to JUnit test class
     * @return JSON formatted string.
     * It contains information of top 3 suspicious score.
     * It is returned to Backend engine of jChecker and stored in Database.
     */
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
        writeJson(sheet, srcPath + "sfl/txt/jchecker.json");
        return sheet;
    }

    private void parseResult(String childPath, ArrayList<Data> suspiciousResult) {
        if (!childPath.endsWith("/")) childPath += "/";
        String subPath = childPath;
        String parentPath = childPath.replace("autoGeneration/", "");
        String sflResultPath = parentPath + "sfl/txt/ochiai.ranking.csv";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(sflResultPath));
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

                BufferedReader srclistReader = new BufferedReader(new FileReader(subPath + "srclist.txt"));
                String absolutePath;
                while ((absolutePath = srclistReader.readLine()) != null) {
                    if (absolutePath.contains(packageName.replace(".", "/") + "/" + fileName + ".java")) break;
                }
                absolutePath = subPath + absolutePath.replace("./", "");
                String fileContents;
                try {
                    byte[] bytes = Files.readAllBytes(new File(absolutePath).toPath());
                    fileContents = new String(bytes, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (fileContents.contains("public static void main")) continue;

                String targetString;
                try (Stream<String> lines = Files.lines(Paths.get(absolutePath))) {
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
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        String sheet = gson.toJson(score);
        return sheet;
    }

    private void writeJson(String sheet, String path) {
        String parentPath = path.replace("autoGeneration/", "");
        File file = new File(parentPath);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(sheet);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
