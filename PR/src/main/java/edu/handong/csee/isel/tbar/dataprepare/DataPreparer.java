package edu.handong.csee.isel.tbar.dataprepare;

import edu.handong.csee.isel.tbar.config.Configuration;
import edu.handong.csee.isel.tbar.utils.FileHelper;
import edu.handong.csee.isel.tbar.utils.JavaLibrary;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataPreparer {
    public String classPath;
    public String srcPath;
    public String testClassPath;
    public String testSrcPath;
    public List<String> libPaths = new ArrayList<>();
    public boolean validPaths = true;
    public String[] testCases;
    public URL[] classPaths;

    public DataPreparer(String path){
        if (!path.endsWith("/")){
            path += "/";
        }
    }

    public void prepareData(String path, String className){
        loadPaths(path, className);

        if (!checkProjectDirectories()){
            validPaths = false;
            return;
        }

        loadTestCases();

        loadClassPaths();
    }

    private void loadPaths(String path, String className) {
        // path = /data/jchecker/{className}/{studentNum}/feedback/{date}/autoGeneration
        String projectDir = Configuration.projectPath; // projectDir = /data/jchecker
        classPath = path + "/bin";
        testClassPath = projectDir + "/data/test/" + className + "/bin";
        srcPath = path + "/src";
        testSrcPath = projectDir + "/data/test/" + className + "/src";

        List<File> libPackages = new ArrayList<>();
        if (new File(projectDir + path + "/lib/").exists()) {
            // ToDO: change the libPackages path
            libPackages.addAll(new FileHelper().getAllFiles(path + "/lib/", ".jar"));
        }
        if (new File(projectDir + path + "/build/lib/").exists()) {
            // ToDO: change the libPackages path
            libPackages.addAll(new FileHelper().getAllFiles(path + "/build/lib/", ".jar"));
        }
        for (File libPackage : libPackages) {
            libPaths.add(libPackage.getAbsolutePath());
        }
    }

    private boolean checkProjectDirectories() {
        if (!new File(classPath).exists()) {
            System.err.println("Class path: " + classPath + " does not exist!");
            return false;
        }
        if (!new File(srcPath).exists()) {
            System.err.println("Source code path: " + srcPath + " does not exist!");
            return false;
        }
        if (!new File(testClassPath).exists()) {
            System.err.println("Test class path: " + testClassPath + " does not exist!");
            return false;
        }
        if (!new File(testSrcPath).exists()) {
            System.err.println("Test source path: " + testSrcPath + " does not exist!");
            return false;
        }
        return true;
    }

    private void loadTestCases() {
        testCases = new TestClassesFinder().findIn(new JavaLibrary().classPathFrom(testClassPath + ":" + classPath), false);
        Arrays.sort(testCases);
    }

    private void loadClassPaths() {
        classPaths = new JavaLibrary().classPathFrom(testClassPath);
        classPaths = new JavaLibrary().extendClassPathWith(classPath, classPaths);
        if (libPaths != null) {
            for (String lpath : libPaths) {
                classPaths = new JavaLibrary().extendClassPathWith(lpath, classPaths);
            }
        }
    }
}
