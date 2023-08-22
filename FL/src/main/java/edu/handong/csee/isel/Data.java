package edu.handong.csee.isel;

/**
 * JSON object which stores information of top 3 suspicious fault location
 * @author nayeawon
 */
public class Data {
    private String line;
    private Float suspiciousValue;
    private int lineNumber;
    private String fileName;

    /**
     * Assign values related to suspicious fault location
     * @param line exact line information
     * @param suspiciousValue suspicious score
     * @param lineNumber line number
     * @param fileName file name
     */
    public Data(String line, Float suspiciousValue, int lineNumber, String fileName) {
        this.line = line.trim();
        this.suspiciousValue = suspiciousValue;
        this.lineNumber = lineNumber;
        this.fileName = fileName;
    }

    /**
     * Getter for exact line information
     * @return exact line information about suspicious fault location.
     */
    public String getLine() { return line; }

    /**
     * Getter for suspicious score
     * @return suspicious score about suspicious fault location.
     */
    public Float getSuspiciousValue() { return suspiciousValue; }

    /**
     * Getter for line number
     * @return line number about suspicious fault location.
     */
    public int getLineNumber() { return lineNumber; }

    /**
     * Getter for file name
     * @return file name about suspicious fault location.
     */
    public String getFileName() { return fileName; }
}
