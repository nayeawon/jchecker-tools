package edu.handong.csee.isel;

import edu.handong.csee.isel.mail.MailUtil;
import edu.handong.csee.isel.tbar.AbstractFixer;
import edu.handong.csee.isel.tbar.TBarFixer;
import edu.handong.csee.isel.tbar.config.Configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TbarRunner {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Help");
            System.exit(0);
        }
        String path = args[0];
        String className = args[1];
        String email = args[2];
        TbarRunner main = new TbarRunner();
        main.fixBug(path, className, email);
    }

    private String processRankingCsv(String path) {
        String targetFile = path + "/ochiai.ranking.csv";
        String processedFile = path + "/intermidiate/ranking.txt";
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(targetFile));
            writer = new BufferedWriter(new FileWriter(processedFile));
            String line = reader.readLine();
            while((line = reader.readLine()) != null) {
                String packageName = line.substring(0, line.indexOf("$"));
                String fileName = line.substring(line.indexOf("$") + 1, line.indexOf("#"));
                String methodName = line.substring(line.indexOf("#") + 1, line.indexOf(":"));
                String lineNumber = line.substring(line.indexOf(":") + 1, line.indexOf(";"));
                String suspiciousValue = line.substring(line.indexOf(";") + 1);
                writer.write(packageName + "." + fileName + "@" + lineNumber + "\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return processedFile;
    }

    private void processFailedTests(String path) {
        String targetFile = path + "/tests.csv";
        String processedFile = path + "/intermediate/tests.txt";
        BufferedReader reader = null;
        BufferedWriter writer = null;
        List<String> failedTests = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(targetFile));
            writer = new BufferedWriter(new FileWriter(processedFile));
            String line = reader.readLine();
            int num = 0;
            while((line = reader.readLine()) != null) {
                String[] parsedLine = line.split(",");
                if (parsedLine[1].equals("FAIL")) {
                    String packageName = parsedLine[0].replace("#", "::");
                    failedTests.add(packageName);
                    num++;
                }
                writer.write("Failing tests: " + num + "\n");
                for (String failedTest : failedTests) {
                    writer.write("  - " + failedTest + "\n");
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void fixBug(String path, String className, String email) {
        String inputPath = path;
        if (path.contains("/autoGeneration/")) {
            path.replace("/autoGeneration/", "");
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // path = /data/jchecker/{className}/{studentNum}/feedback/{date}/autoGeneration
                Configuration.outputPath = path + "/pr";
                String suspiciousFileStr = processRankingCsv(path + "/sfl/txt");
                processFailedTests(path + "/sfl/txt");
                AbstractFixer fixer = new TBarFixer(inputPath, className);
                fixer.dataType = "TBar";
                fixer.metric = Configuration.faultLocalizationMetric;
                fixer.suspCodePosFile = new File(suspiciousFileStr);
                fixer.fixProcess();
                int fixedStatus = fixer.fixedStatus;
                return fixedStatus;
            }
        };
        Future<Object> future = executor.submit(task);
        Object result = 0;
        try {
            result = future.get(1, TimeUnit.HOURS);
        } catch (TimeoutException e) {
            result = 0;
        } catch (InterruptedException e) {
            result = 0;
        } catch (ExecutionException e) {
            result = 0;
        } finally {
            future.cancel(true);
        }

        switch ((int) result) {
            case 0:
                System.out.println("Failed to fix bug " + className + ":" + path);
                new MailUtil(false).run(path, email);
                break;
            case 1:
                System.out.println("Succeeded to fix bug " + className + ":" + path);
                new MailUtil(true).run(path + "/pr/FixedBugs", email);
                break;
            case 2:
                System.out.println("Partial succeeded to fix bug " + className + ":" + path);
                new MailUtil(true).run(path + "/pr/PartiallyFixedBugs", email);
                break;
        }
    }
}
