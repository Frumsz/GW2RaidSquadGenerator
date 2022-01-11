package com.crossroadsinn.problem;

public interface CSP {

    CSP getChildren() throws Exception;

    int heuristic();

    boolean isSolution();
}
