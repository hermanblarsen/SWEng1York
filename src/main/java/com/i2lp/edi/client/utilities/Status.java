package com.i2lp.edi.client.utilities;

/**
 * Created by Luke on 20/04/2017.
 */
public class Status {

    private String xName, xValue, yName, yValue, zName, zValue;

    public Status(String xName, String xValue, String yName, String yValue, String zName, String zValue) {
        this.xName = xName;
        this.xValue = xValue;
        this.yName = yName;
        this.yValue = yValue;
        this.zName = zName;
        this.zValue = zValue;
    }

    public String getxName() {
        return xName;
    }

    public String getxValue() {
        return xValue;
    }

    public String getyName() {
        return yName;
    }

    public String getyValue() {
        return yValue;
    }

    public String getzName() {
        return zName;
    }

    public String getzValue() {
        return zValue;
    }

    public void setxName(String xName) {
        this.xName = xName;
    }

    public void setxValue(String xValue) {
        this.xValue = xValue;
    }

    public void setyName(String yName) {
        this.yName = yName;
    }

    public void setyValue(String yValue) {
        this.yValue = yValue;
    }

    public void setzName(String zName) {
        this.zName = zName;
    }

    public void setzValue(String zValue) {
        this.zValue = zValue;
    }
}

