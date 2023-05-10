package edu.handong.csee.isel.tbar.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestUtils {
    public static int getFailTestNumInProject(String path, List<String> failedTests) {
        return FileUtils.getGZoltarResultFromFile(path);
    }
    public static String readPatch(String projectName) {
        try {
            String buggyProject = projectName.substring(projectName.lastIndexOf("/") + 1);
            return ShellUtils.shellRun(Arrays.asList("cd " + projectName + "\n", "git diff"), buggyProject, 1).trim();
        } catch (IOException e){
            return null;
        }
    }
}
