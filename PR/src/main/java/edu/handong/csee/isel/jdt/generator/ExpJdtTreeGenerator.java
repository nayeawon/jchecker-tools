package edu.handong.csee.isel.jdt.generator;

import edu.handong.csee.isel.jdt.visitor.AbstractJdtVisitor;
import edu.handong.csee.isel.jdt.visitor.ExpJdtVisitor;

public class ExpJdtTreeGenerator extends AbstractJdtTreeGenerator{
    @Override
    protected AbstractJdtVisitor createVisitor() {
        return new ExpJdtVisitor();
    }
}
