package com.crossroadsinn.problem;

public class SearchResultsState {
    private int failures = 0;

    public SearchResultsState(){

    }

    public int getFailures() {
        return failures;
    }

    public void incrementFailures() {
        failures++;
    }

}
