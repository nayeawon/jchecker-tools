package edu.handong.csee.isel;

public class Data {
    private String line;
    private Float suspiciousValue;
    private int lineNumber;
    private String fileName;

    public Data(String line, Float suspiciousValue, int lineNumber, String fileName) {
        this.line = line.trim();
        this.suspiciousValue = suspiciousValue;
        this.lineNumber = lineNumber;
        this.fileName = fileName;
    }

    public String getLine() { return line; }
    public Float getSuspiciousValue() { return suspiciousValue; }

    public int getLineNumber() { return lineNumber; }

    public String getFileName() { return fileName; }
}
