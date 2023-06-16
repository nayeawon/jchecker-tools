package edu.handong.csee.isel;

import edu.handong.csee.isel.mail.MailUtil;
import edu.handong.csee.isel.tbar.AbstractFixer;
import edu.handong.csee.isel.tbar.TBarFixer;
import edu.handong.csee.isel.tbar.config.Configuration;
import edu.handong.csee.isel.tbar.utils.jCheckerUtils;

import java.io.*;
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

    public void fixBug(String path, String className, String email) {
        if (path.contains("/autoGeneration/")) {
            path = path.replace("/autoGeneration/", "");
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        String finalPath = path;
        Callable<Object> task = () -> {
            // path = /data/jchecker/{className}/{studentNum}/feedback/{date}/autoGeneration
            Configuration.outputPath = finalPath + "/tbar";
            String suspiciousFileStr = jCheckerUtils.processRankingCsv(finalPath + "/sfl/txt");
            jCheckerUtils.processFailedTests(finalPath + "/sfl/txt");
            AbstractFixer fixer = new TBarFixer(finalPath, className);
            fixer.dataType = "TBar";
            fixer.metric = Configuration.faultLocalizationMetric;
            fixer.suspCodePosFile = new File(suspiciousFileStr);
            fixer.fixProcess();
            int fixedStatus = fixer.fixedStatus;
            return fixedStatus;
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
                new MailUtil(true).run(path + "/tbar/FixedBugs", email);
                break;
            case 2:
                System.out.println("Partial succeeded to fix bug " + className + ":" + path);
                new MailUtil(true).run(path + "/tbar/PartiallyFixedBugs", email);
                break;
        }
    }
}

