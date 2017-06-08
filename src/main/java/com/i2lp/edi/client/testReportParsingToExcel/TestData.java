package com.i2lp.edi.client.testReportParsingToExcel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by amriksadhra on 03/06/2017.
 */

/**
 * Class to parse test data to HTML for use in report generation
 */
public class TestData {
    private Date testDate;
    private String commitHash;
    private int numTests;
    private int numFailures;
    private int numErrors;
    private int numSkipped;

    private Double successRate;

    public TestData(Date testDate, String commitHash, Integer numTests, Integer numFailures, Integer numErrors, Integer numSkipped, Double successRate) {
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
        LocalDateTime ldt = LocalDateTime.ofInstant(testDate.toInstant(), ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");
        return commitHash + ";" + ldt.format(dtf) + ";" + numTests + ";" + numFailures + ";" + numErrors + ";" + numSkipped;
    }
}
