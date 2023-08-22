package edu.handong.csee.isel;

/**
 * Handle command line interface inputs
 * @author nayeawon
 */
public class Main {

    /**
     * Executed by command line interface
     * @param args paths to required classes
     *             example: ./Gzoltar-jchecker "path-to-source-class" "path-to-JUnit-test-class"
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.run(args[0], args[1]);
    }

    /**
     * Execute GZoltarRunner
     * @param srcPath path to source classes
     * @param testPath path to JUnit test class
     * @return result of GZoltarRunner in string.
     */
    public String run(String srcPath, String testPath) {
        GZoltarRunner gzoltar = new GZoltarRunner();
        String result = gzoltar.run(srcPath, testPath);
        return result;
    }
}
