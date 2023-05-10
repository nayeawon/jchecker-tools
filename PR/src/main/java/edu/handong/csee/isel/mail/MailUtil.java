package edu.handong.csee.isel.mail;

import java.io.File;
import java.util.ArrayList;

public class MailUtil {
    private static final String mailSuccessPath = "/home/DPMiner/lib/Mail-success.sh";
    private static final String mailFailPath = "/home/DPMiner/lib/Mail-fail.sh";
    private boolean succeed;

    public MailUtil(Boolean succeed) {
        this.succeed = succeed;
    }

    public void run(String srcPath, String email) {
        ArrayList<String> command = new ArrayList<>();
        if (succeed) command.add(mailSuccessPath);
        else command.add(mailFailPath);
        command.add(srcPath);
        command.add(email);

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
    }
}
