package com.i2lp.edi.client.testparser;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by amriksadhra on 03/06/2017.
 */
public class TestParser {
    public static void main(String[] args) {
        String path = args[0];
        ArrayList<File> testFiles = getFilesInFolder(path);
        ArrayList<TestData> testDatas = new ArrayList<>();

        for (File testFile : testFiles) {
            TestData testData = getCommitHashAndDateTime(testFile.getName());
            String htmlContent = getHTMLContent(testFile.getPath());
            testData = getTestData(testData, htmlContent);
            if (testData != null) {
                testDatas.add(testData);
            }
        }

        writeTestDataToCSV(testDatas, path + File.separator + "TestData.csv");
    }

    private static void writeTestDataToCSV(ArrayList<TestData> testDataArrayList, String filePath) {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(filePath);
            fileWriter.append("commitHash;testTime;numTests;numFailures;numErrors;numSkipped;successRate" + "\n");
            for (TestData testData : testDataArrayList) {
                fileWriter.append(testData.toString() + "\n");
            }
            System.out.println("CSV File Created");
        } catch (Exception e) {
            System.out.println("Error Writing CSV");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }

    private static TestData getTestData(TestData toAppend, String htmlContent) {
        String testDataString = htmlContent.substring(htmlContent.indexOf("<tr class=\"b\">") + 18, htmlContent.indexOf("</tr></table><br />"));
        String commaDelimit = testDataString.replace("<td>", ",").replace("</td>", "");

        Double[] testValues = new Double[6];
        int i = 0;
        Scanner scanner = new Scanner(commaDelimit).useDelimiter(",");

        while (scanner.hasNext()) {
            String data = scanner.next();
            testValues[i] = Double.parseDouble(data.replace("%", ""));
            i++;
        }
        scanner.close();

        if (testValues[0] != 0) {
            return new TestData(toAppend.getTestDate(), toAppend.getCommitHash(), testValues[0].intValue(), testValues[1].intValue(), testValues[2].intValue(), testValues[3].intValue(), testValues[4]);
        } else {
            return null;
        }
    }

    private static TestData getCommitHashAndDateTime(String fileName) {
        Date testDate = new Date(Integer.parseInt(fileName.substring(0, 4)) - 1900,
                Integer.parseInt(fileName.substring(5, 7)) - 1,
                Integer.parseInt(fileName.substring(8, 10)),
                Integer.parseInt(fileName.substring(12, 14)),
                Integer.parseInt(fileName.substring(15, 17)),
                Integer.parseInt(fileName.substring(18, 20)));

        String commitHash = fileName.substring(21, 61);

        return new TestData(testDate, commitHash);
    }


    public static ArrayList<File> getFilesInFolder(String path) {
        ArrayList<File> filesInFolder = new ArrayList<>();
        //Set target path to read list of present files from
        final File folder = new File(path);

        //Generate array of files in folder
        for (File fileEntry : folder.listFiles()) {
            if (!(fileEntry.getName().contains(".DS_"))) filesInFolder.add(fileEntry);
        }

        return filesInFolder;
    }

    public static String getHTMLContent(String path) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Couldn't parse HTML into String");
        }

        return contentBuilder.toString();
    }
}
