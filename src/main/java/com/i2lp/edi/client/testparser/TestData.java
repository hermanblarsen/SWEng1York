package com.i2lp.edi.client.testparser;

import java.util.Date;

/**
 * Created by amriksadhra on 03/06/2017.
 */
public class TestData {
    private Date testDate;
    private String commitHash;
    private int numTests;
    private int numFailures;
    private int numErrors;

    private Double successRate;

    public TestData(Date testDate, String commitHash, Integer numTests, Integer numFailures, Integer numErrors, Double successRate) {
        this.testDate = testDate;
        this.commitHash = commitHash;
        this.numTests = numTests;
        this.numFailures = numFailures;
        this.numErrors = numErrors;
        this.successRate = successRate;
    }

    public TestData(Date testDate, String commitHash){
        this.testDate = testDate;
        this.commitHash = commitHash;
    }


    public String getCommitHash() {
        return commitHash;
    }

    public Date getTestDate() {
        return testDate;
    }

    public int getNumTests() {
        return numTests;
    }

    public int getNumFailures() {
        return numFailures;
    }

    public int getNumErrors() {
        return numErrors;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public String toString(){
        return commitHash + ", " + testDate.toString() + ", " + numTests + ", " + numFailures + ", " + numErrors + ", " + successRate;
    }
}
