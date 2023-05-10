package edu.handong.csee.isel.tbar.dataprepare;

public interface ClassFilter {
    boolean acceptClass(Class<?> clazz);

    boolean acceptClassName(String className);

    boolean acceptInnerClass();

    boolean searchInJars();
}
