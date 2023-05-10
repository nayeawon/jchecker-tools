package edu.handong.csee.isel.tbar;

import edu.handong.csee.isel.tbar.utils.SuspiciousPosition;
import edu.handong.csee.isel.tbar.AbstractFixer.SuspCodeNode;

import java.util.List;

public interface IFixer {
    List<SuspiciousPosition> readSuspiciousCodeFromFile();
    List<SuspCodeNode> parseSuspiciousCode(SuspiciousPosition suspiciousCode);
    void fixProcess();
}
