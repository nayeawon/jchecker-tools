package edu.handong.csee.isel.tbar;

import edu.handong.csee.isel.jdt.tree.ITree;
import edu.handong.csee.isel.tbar.config.Configuration;
import edu.handong.csee.isel.tbar.context.Dictionary;
import edu.handong.csee.isel.tbar.dataprepare.DataPreparer;
import edu.handong.csee.isel.tbar.info.Patch;
import edu.handong.csee.isel.tbar.utils.FileHelper;
import edu.handong.csee.isel.tbar.utils.PathUtils;
import edu.handong.csee.isel.tbar.utils.ShellUtils;
import edu.handong.csee.isel.tbar.utils.TestUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractFixer implements IFixer{

    private Logger log = LoggerFactory.getLogger(AbstractFixer.class);
    public String metric = "Ochiai";          // Fault localization metric.
    protected String path = "";
    protected String buggyProject = "";     // The buggy project name.
    public int minErrorTest;                // Number of failed test cases before fixing.
    public int minErrorTest_;
    protected int minErrorTestAfterFix = 0; // Number of failed test cases after fixing
    protected String fullBuggyProjectPath;  // The full path of the local buggy project.
    public File suspCodePosFile = null;     // The file containing suspicious code positions localized by FL tools.
    protected DataPreparer dp;
    private String failedTestCaseClasses = ""; // Classes of the failed test cases before fixing.
    protected List<String> failedTestStrList = new ArrayList<>();
    protected List<String> failedTestCasesStrList = new ArrayList<>();
    private List<String> fakeFailedTestCasesList = new ArrayList<>();
    public int fixedStatus = 0;
    public String dataType = "";
    protected int patchId = 0;
    protected int comparablePatches = 0;
    protected Dictionary dic = null;

    public boolean isTestFixPatterns = false;

    public AbstractFixer(String path, String className) {
        this.path = path;
        minErrorTest = TestUtils.getFailTestNumInProject(path, failedTestStrList);
        log.info("Failed Tests: " + minErrorTest + "(" + className + ":" + path + ")");
        minErrorTest_ = minErrorTest;

        this.dp = new DataPreparer(path);
        dp.prepareData(path, className);

        readPreviouslyFailedTestCases(path, className);
    }

    private void readPreviouslyFailedTestCases(String path, String className) {
        String[] failedTestCases = new FileHelper().readFile(path + "/sfl/txt/intermediate/tests.txt").split("\n");
        List<String> failedTestCasesList = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (int index = 1, length = failedTestCases.length; index < length; index++) {
            // - org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731_2
            String failedTestCase = failedTestCases[index].trim();
            failed.add(failedTestCase);
            failedTestCase = failedTestCase.substring(failedTestCase.indexOf("-") + 1).trim();
            failedTestCasesStrList.add(failedTestCase);
            int colonIndex = failedTestCase.indexOf("::");
            if (colonIndex > 0) {
                failedTestCase = failedTestCase.substring(0, colonIndex);
            }
            if (!failedTestCasesList.contains(failedTestCase)) {
                this.failedTestCaseClasses += failedTestCase + " ";
                failedTestCasesList.add(failedTestCase);
            }
        }

        List<String> tempFailed = new ArrayList<>();
        tempFailed.addAll(this.failedTestStrList);
        tempFailed.removeAll(failed);
        // FIXME: Using defects4j command in Java code may generate some new failed-passing test cases.
        // We call them as fake failed-passing test cases.
        this.fakeFailedTestCasesList.addAll(tempFailed);
    }

    protected List<Patch> triedPatchCandidates = new ArrayList<>();

    protected void testGeneratedPatches(List<Patch> patchCandidates, SuspCodeNode scn) {
        // ToDO: change to make it work in jChecker
        // Testing generated patches.
        for (Patch patch : patchCandidates) {
            patch.buggyFileName = scn.suspiciousJavaFile;
            addPatchCodeToFile(scn, patch);// Insert the patch.
            if (this.triedPatchCandidates.contains(patch)) continue;
            patchId++;
            if (patchId > 10000) return;
            this.triedPatchCandidates.add(patch);

            String buggyCode = patch.getBuggyCodeStr();
            if ("===StringIndexOutOfBoundsException===".equals(buggyCode)) continue;
            String patchCode = patch.getFixedCodeStr1();
            scn.targetClassFile.delete();

            log.debug("Compiling");
            try {// Compile patched file.
                ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.7 -target 1.7 -cp "
                        + PathUtils.buildCompileClassPath(Arrays.asList(PathUtils.getJunitPath()), dp.classPath, dp.testClassPath)
                        + " -d " + dp.classPath + " " + scn.targetJavaFile.getAbsolutePath()), buggyProject, 1);
            } catch (IOException e) {
                log.debug(buggyProject + " ---Fixer: fix fail because of javac exception! ");
                continue;
            }
            if (!scn.targetClassFile.exists()) { // fail to compile
                log.debug(buggyProject + " ---Fixer: fix fail because of failed compiling! ");
                continue;
            }
            log.debug("Finish of compiling.");
            comparablePatches++;

            log.debug("Test previously failed test cases.");
            try {
                String results = ShellUtils.shellRun(Arrays.asList("java -cp "
                        + PathUtils.buildTestClassPath(dp.classPath, dp.testClassPath)
                        + " org.junit.runner.JUnitCore " + this.failedTestCaseClasses), buggyProject, 2);

                if (results.isEmpty()) {
                    continue;
                } else {
                    if (!results.contains("java.lang.NoClassDefFoundError")) {
                        List<String> tempFailedTestCases = readTestResults(results);
                        tempFailedTestCases.retainAll(this.fakeFailedTestCasesList);
                        if (!tempFailedTestCases.isEmpty()) {
                            if (this.failedTestCasesStrList.size() == 1) continue;

                            // Might be partially fixed.
                            tempFailedTestCases.removeAll(this.failedTestCasesStrList);
                            if (!tempFailedTestCases.isEmpty()) continue; // Generate new bugs.
                        }
                    }
                }
            } catch (IOException e) { }

            List<String> failedTestsAfterFix = new ArrayList<>();
            int errorTestAfterFix = TestUtils.getFailTestNumInProject(fullBuggyProjectPath, failedTestsAfterFix);
            failedTestsAfterFix.removeAll(this.fakeFailedTestCasesList);

            if (errorTestAfterFix < minErrorTest) {
                List<String> tmpFailedTestsAfterFix = new ArrayList<>();
                tmpFailedTestsAfterFix.addAll(failedTestsAfterFix);
                tmpFailedTestsAfterFix.removeAll(this.failedTestStrList);
                if (tmpFailedTestsAfterFix.size() > 0) { // Generate new bugs.
                    log.debug(buggyProject + " ---Generated new bugs: " + tmpFailedTestsAfterFix.size());
                    continue;
                }

                // Output the generated patch.
                if (errorTestAfterFix == 0 || failedTestsAfterFix.isEmpty()) {
                    fixedStatus = 1;
                    log.info("Succeeded to fix the bug " + buggyProject + "====================");
                    String patchStr = TestUtils.readPatch(this.fullBuggyProjectPath);
                    System.out.println(patchStr);
                    if (patchStr == null || !patchStr.startsWith("diff")) {
                        FileHelper.outputToFile(Configuration.outputPath + "/FixedBugs/Patch_" + patchId + "_" + comparablePatches + ".txt",
                                "//**********************************************************\n//" + scn.suspiciousJavaFile + " ------ " + scn.buggyLine
                                        + "\n//**********************************************************\n"
                                        + "===Buggy Code===\n" + buggyCode + "\n\n===Patch Code===\n" + patchCode, false);
                    } else {
                        FileHelper.outputToFile(Configuration.outputPath + "/FixedBugs/Patch_" + patchId + "_" + comparablePatches + ".txt", patchStr, false);
                    }

                    if (!isTestFixPatterns) {
                        this.minErrorTest = 0;
                        break;
                    }
                } else {
                    if (minErrorTestAfterFix == 0 || errorTestAfterFix <= minErrorTestAfterFix) {
                        minErrorTestAfterFix = errorTestAfterFix;
                        fixedStatus = 2;
                        minErrorTest_ = minErrorTest_ - (minErrorTest - errorTestAfterFix);
                        if (minErrorTest_ <= 0) {
                            fixedStatus = 1;
                            minErrorTest = 0;
                        }
                        log.info("Partially Succeeded to fix the bug " + buggyProject + "====================");
                        String patchStr = TestUtils.readPatch(this.fullBuggyProjectPath);
                        if (patchStr == null || !patchStr.startsWith("diff")) {
                            FileHelper.outputToFile(Configuration.outputPath + "/PartiallyFixedBugs/Patch_" + patchId + "_" + comparablePatches + ".txt",
                                    "//**********************************************************\n//" + scn.suspiciousJavaFile + " ------ " + scn.buggyLine
                                            + "\n//**********************************************************\n"
                                            + "===Buggy Code===\n" + buggyCode + "\n\n===Patch Code===\n" + patchCode, false);
                        } else {
                            FileHelper.outputToFile(Configuration.outputPath + "/PartiallyFixedBugs/Patch_" + patchId + "_" + comparablePatches + ".txt", patchStr, false);
                        }
                        break;
                    }
                }
            } else {
                log.debug("Failed Tests after fixing: " + errorTestAfterFix + " " + buggyProject);
            }
        }

        try {
            scn.targetJavaFile.delete();
            scn.targetClassFile.delete();
            Files.copy(scn.javaBackup.toPath(), scn.targetJavaFile.toPath());
            Files.copy(scn.classBackup.toPath(), scn.targetClassFile.toPath());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void addPatchCodeToFile(SuspCodeNode scn, Patch patch) {
        String javaCode = FileHelper.readFile(scn.javaBackup);

        String fixedCodeStr1 = patch.getFixedCodeStr1();
        String fixedCodeStr2 = patch.getFixedCodeStr2();
        int exactBuggyCodeStartPos = patch.getBuggyCodeStartPos();
        int exactBuggyCodeEndPos = patch.getBuggyCodeEndPos();
        String patchCode = fixedCodeStr1;
        boolean needBuggyCode = false;
        if (exactBuggyCodeEndPos > exactBuggyCodeStartPos) {
            if ("MOVE-BUGGY-STATEMENT".equals(fixedCodeStr2)) {
                // move statement position.
            } else if (exactBuggyCodeStartPos != -1 && exactBuggyCodeStartPos < scn.startPos) {
                // Remove the buggy method declaration.
            } else {
                needBuggyCode = true;
                if (exactBuggyCodeStartPos == 0) {
                    // Insert the missing override method, the buggy node is TypeDeclaration.
                    int pos = scn.suspCodeAstNode.getPos() + scn.suspCodeAstNode.getLength() - 1;
                    for (int i = pos; i >= 0; i --) {
                        if (javaCode.charAt(i) == '}') {
                            exactBuggyCodeStartPos = i;
                            exactBuggyCodeEndPos = i + 1;
                            break;
                        }
                    }
                } else if (exactBuggyCodeStartPos == -1 ) {
                    // Insert generated patch code before the buggy code.
                    exactBuggyCodeStartPos = scn.startPos;
                    exactBuggyCodeEndPos = scn.endPos;
                } else {
                    // Insert a block-held statement to surround the buggy code
                }
            }
        } else if (exactBuggyCodeStartPos == -1 && exactBuggyCodeEndPos == -1) {
            // Replace the buggy code with the generated patch code.
            exactBuggyCodeStartPos = scn.startPos;
            exactBuggyCodeEndPos = scn.endPos;
        } else if (exactBuggyCodeStartPos == exactBuggyCodeEndPos) {
            // Remove buggy variable declaration statement.
            exactBuggyCodeStartPos = scn.startPos;
        }

        patch.setBuggyCodeStartPos(exactBuggyCodeStartPos);
        patch.setBuggyCodeEndPos(exactBuggyCodeEndPos);
        String buggyCode;
        try {
            buggyCode = javaCode.substring(exactBuggyCodeStartPos, exactBuggyCodeEndPos);
            if (needBuggyCode) {
                patchCode += buggyCode;
                if (fixedCodeStr2 != null) {
                    patchCode += fixedCodeStr2;
                }
            }

            File newFile = new File(scn.targetJavaFile.getAbsolutePath() + ".temp");
            String patchedJavaFile = javaCode.substring(0, exactBuggyCodeStartPos) + patchCode + javaCode.substring(exactBuggyCodeEndPos);
            FileHelper.outputToFile(newFile, patchedJavaFile, false);
            newFile.renameTo(scn.targetJavaFile);
        } catch (StringIndexOutOfBoundsException e) {
            log.debug(exactBuggyCodeStartPos + " ==> " + exactBuggyCodeEndPos + " : " + javaCode.length());
            e.printStackTrace();
            buggyCode = "===StringIndexOutOfBoundsException===";
        }

        patch.setBuggyCodeStr(buggyCode);
        patch.setFixedCodeStr1(patchCode);
    }

    public class SuspCodeNode {

        public File javaBackup;
        public File classBackup;
        public File targetJavaFile;
        public File targetClassFile;
        public int startPos;
        public int endPos;
        public ITree suspCodeAstNode;
        public String suspCodeStr;
        public String suspiciousJavaFile;
        public int buggyLine;

        public SuspCodeNode(File javaBackup, File classBackup, File targetJavaFile, File targetClassFile, int startPos,
                            int endPos, ITree suspCodeAstNode, String suspCodeStr, String suspiciousJavaFile, int buggyLine) {
            this.javaBackup = javaBackup;
            this.classBackup = classBackup;
            this.targetJavaFile = targetJavaFile;
            this.targetClassFile = targetClassFile;
            this.startPos = startPos;
            this.endPos = endPos;
            this.suspCodeAstNode = suspCodeAstNode;
            this.suspCodeStr = suspCodeStr;
            this.suspiciousJavaFile = suspiciousJavaFile;
            this.buggyLine = buggyLine;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof SuspCodeNode) {
                SuspCodeNode suspN = (SuspCodeNode) obj;
                if (startPos != suspN.startPos) return false;
                if (endPos != suspN.endPos) return false;
                if (suspiciousJavaFile.equals(suspN.suspiciousJavaFile)) return true;
            }
            return false;
        }
    }

    private List<String> readTestResults(String results) {
        List<String> failedTeatCases = new ArrayList<>();
        String[] testResults = results.split("\n");
        for (String testResult : testResults) {
            if (testResult.isEmpty()) continue;

            if (NumberUtils.isDigits(testResult.substring(0, 1))) {
                int index = testResult.indexOf(") ");
                if (index <= 0) continue;
                testResult = testResult.substring(index + 1, testResult.length() - 1).trim();
                int indexOfLeftParenthesis = testResult.indexOf("(");
                if (indexOfLeftParenthesis < 0) {
                    System.err.println(testResult);
                    continue;
                }
                String testCase = testResult.substring(0, indexOfLeftParenthesis);
                String testClass = testResult.substring(indexOfLeftParenthesis + 1);
                failedTeatCases.add(testClass + "::" + testCase);
            }
        }
        return failedTeatCases;
    }
}
