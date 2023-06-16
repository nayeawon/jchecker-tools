package edu.handong.csee.isel.tbar.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestUtils {
    public static int getFailTestNumInProject(String path, List<String> failedTests) {
        return FileUtils.getGZoltarResultFromFile(path, failedTests);
    }
}
