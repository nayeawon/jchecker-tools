package edu.handong.csee.isel.tbar.config;

public class Configuration {
    public static String projectPath = "/data/jchecker2.0";
    public static String knownBugPositions = "";
    public static String suspPositionsFilePath = "SuspiciousCodePositions";
    public static String faultLocalizationMetric = "Ochiai";
    public static String outputPath = "";

    public static final String TEMP_FILES_PATH = ".temp/";
    public static final long SHELL_RUN_TIMEOUT = 300L;
    public static final long TEST_SHELL_RUN_TIMEOUT = 600L;
}
