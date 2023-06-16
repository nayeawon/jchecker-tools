package edu.handong.csee.isel.mail;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MailUtil {
    private static final String mailSuccessPath = "/home/DPMiner/lib/Mail-success.sh";
    private static final String mailFailPath = "/home/DPMiner/lib/Mail-fail.sh";
    private boolean succeed;

    private boolean isKorean;

    public MailUtil(Boolean succeed) {
        this.succeed = succeed;
    }

    private String runGPT(String patch) {

        ProcessBuilder processBuilder = new ProcessBuilder(getGPTCommand(patch));
        Process process = null;
        String contents ="";
        try {
            process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            contents = line;
            while((line = reader.readLine()) != null) {
                contents += line;
            }
            process.waitFor();
            int exitCode = process.exitValue();
            if (exitCode != 0)
                throw new InterruptedException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            process.destroy();
        }

        if (contents.length() < 1) return "";
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(contents);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonObj = (JSONObject)obj;
        JSONArray jsonArr = (JSONArray)jsonObj.get("choices");
        if (jsonArr == null) return "";
        jsonObj = (JSONObject) jsonArr.get(0);
        Map address = ((Map)jsonObj.get("message"));
        Iterator<Map.Entry> itr1 = address.entrySet().iterator();
        while (itr1.hasNext()) {
            Map.Entry pair = itr1.next();
            if (pair.getKey().equals("content")) return (String)pair.getValue();
        }
        return "";
    }

    public void run(String srcPath, String email) {
        if (!succeed) {
            sendEmail("", email);
            return;
        }

        if (srcPath.contains("Kor")) isKorean = true;

        File dir = new File(srcPath);
        String fileContents = "";
        for (File file : dir.listFiles()) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                fileContents = new String(bytes, StandardCharsets.UTF_8).replace("\n", "\\n");
                String answer = runGPT(fileContents);
                if (answer.length() < 1) continue;
                fileContents.replace("\\n", "\n");
                String parsedAnswer = "Explanation:\n" + answer + "\n\n\nPatch:\n" + fileContents.replace("\\n", "\n");
                sendEmail(parsedAnswer, email);
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        sendEmail("Patch:\n" + fileContents.replace("\\n", "\n"), email);
    }

    private void sendEmail(String content, String email) {

        ProcessBuilder builder;
        Process process = null;

        try {
            builder = new ProcessBuilder(getCommand(content, email));
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

    private ArrayList<String> getGPTCommand(String patch) {
        ArrayList<String> command = new ArrayList<>();
        command.add("bash");
        command.add("-c");
        if (isKorean) {
            command.add("curl https://api.openai.com/v1/chat/completions -H 'Content-Type: application/json' -H 'Authorization: Bearer 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' "
                    + "-d '{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\",\"content\": \"You are a teaching assistant of a java programming class. Please explain the following patch in Korean. "
                    + patch + "\"}]}'");
        } else
        command.add("curl https://api.openai.com/v1/chat/completions -H 'Content-Type: application/json' -H 'Authorization: Bearer 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' "
                + "-d '{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\",\"content\": \"You are a teaching assistant of a java programming class. Please elaborate the following patch. "
                + patch + "\"}]}'");
        return command;
    }

    private ArrayList<String> getCommand(String content, String email) {
        ArrayList<String> command = new ArrayList<>();
        if (!succeed) {
            command.add(mailFailPath);
        } else {
            command.add(mailSuccessPath);
            command.add(content);
        }
        command.add(email);
        return command;
    }
}
