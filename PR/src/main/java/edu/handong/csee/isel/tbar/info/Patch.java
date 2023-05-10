package edu.handong.csee.isel.tbar.info;

public class Patch {
    private String buggyCodeStr = "";
    private String fixedCodeStr1 = "";
    private String fixedCodeStr2 = null;
    private int buggyCodeStartPos = -1;
    private int buggyCodeEndPos = -1;
    public String buggyFileName;

    public String getBuggyCodeStr() {
        return buggyCodeStr;
    }

    public void setBuggyCodeStr(String buggyCodeStr) {
        this.buggyCodeStr = buggyCodeStr;
    }

    public String getFixedCodeStr1() {
        return fixedCodeStr1;
    }

    public void setFixedCodeStr1(String fixedCodeStr1) {
        this.fixedCodeStr1 = fixedCodeStr1;
    }

    public String getFixedCodeStr2() {
        return fixedCodeStr2;
    }

    public void setFixedCodeStr2(String fixedCodeStr2) {
        this.fixedCodeStr2 = fixedCodeStr2;
    }

    public int getBuggyCodeStartPos() {
        return buggyCodeStartPos;
    }

    public void setBuggyCodeStartPos(int buggyCodeStartPos) {
        this.buggyCodeStartPos = buggyCodeStartPos;
    }

    public int getBuggyCodeEndPos() {
        return buggyCodeEndPos;
    }

    public void setBuggyCodeEndPos(int buggyCodeEndPos) {
        this.buggyCodeEndPos = buggyCodeEndPos;
    }

    @Override
    public String toString() {
        return this.fixedCodeStr1 + "\n" + this.fixedCodeStr2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Patch) {
            Patch p = (Patch) obj;
            if (!buggyFileName.equals(p.buggyFileName)) return false;
            if (!buggyCodeStr.equals(p.buggyCodeStr)) return false;
            if (buggyCodeStartPos != p.buggyCodeStartPos) return false;
            if (buggyCodeEndPos != p.buggyCodeEndPos) return false;
            if (!fixedCodeStr1.equals(p.fixedCodeStr1)) return false;
            if (fixedCodeStr2 == null) {
                if (p.fixedCodeStr2 != null) return false;
            } else if (!fixedCodeStr2.equals(p.fixedCodeStr2)) return false;
            return true;
        } else return false;
    }
}
