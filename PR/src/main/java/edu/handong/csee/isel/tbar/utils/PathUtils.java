package edu.handong.csee.isel.tbar.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PathUtils {
    public static ArrayList<String> getSrcPath(String className) {
        ArrayList<String> path = new ArrayList<String>();
        path.add("/data/test/" + className + "/bin/"); // testClassPath : /data/jchecker/data/test/{className}/bin/
        path.add("/data/test/" + className); // testSrcPath : /data/jchecker/data/test/{className}
        return path;
    }

    public static String getJunitPath() {
        return System.getProperty("user.dir")+"/target/dependency/junit-4.12.jar";
    }

    private static String getHamcrestPath() {
        return System.getProperty("user.dir")+"/target/dependency/hamcrest-all-1.3.jar";
    }

    public static String buildCompileClassPath(List<String> additionalPath, String classPath, String testClassPath){
        String path = "\"";
        path += classPath;
        path += System.getProperty("path.separator");
        path += testClassPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    public static String buildTestClassPath(String classPath, String testClassPath) {
        String path = "\"";
        path += classPath;
        path += System.getProperty("path.separator");
        path += testClassPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += getJunitPath();
        path += System.getProperty("path.separator");
        path += getHamcrestPath();
        path += System.getProperty("path.separator");
        path += "\"";
        return path;
    }
}
