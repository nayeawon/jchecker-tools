package edu.handong.csee.isel.tbar.fixtemplate;

import edu.handong.csee.isel.jdt.tree.ITree;
import edu.handong.csee.isel.tbar.context.Dictionary;
import edu.handong.csee.isel.tbar.info.Patch;

import java.util.List;

public interface IFixTemplate {
    void setSuspiciousCodeStr(String suspiciousCodeStr);

    String getSuspiciousCodeStr();

    void setSuspiciousCodeTree(ITree suspiciousCodeTree);

    ITree getSuspiciousCodeTree();

    void generatePatches();

    List<Patch> getPatches();

    String getSubSuspiciouCodeStr(int startPos, int endPos);

    void setDictionary(Dictionary dic);
}
