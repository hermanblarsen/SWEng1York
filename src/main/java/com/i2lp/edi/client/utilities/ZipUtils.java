package com.i2lp.edi.client.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private Logger logger = LoggerFactory.getLogger(ZipUtils.class);
    private static Logger loggerStatic = LoggerFactory.getLogger(ZipUtils.class);
    private List<String> fileList;

    public ZipUtils(String sourceFolder, String outputZip){
        fileList = new ArrayList<>();
        this.generateFileList(sourceFolder, new File(sourceFolder));
        zipIt(sourceFolder, outputZip);
    }

    /**
     * Zip it
     *
     * @param zipFile output ZIP file location
     */
    public void zipIt(String sourceFolder, String zipFile) {

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            logger.info("Output to Zip: " + zipFile);

            for (String file : this.fileList) {

                logger.debug("File Added: " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(sourceFolder + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param node file or directory
     */
    public void generateFileList(String sourceFolder, File node) {

        //add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(sourceFolder, node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(sourceFolder, new File(node, filename));
            }
        }

    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String sourceFolder, String file) {
        return file.substring(sourceFolder.length() + 1, file.length());
    }

    /**
     * Unzip downloaded zip file to Presentations folder, and then delete the downloaded zip
     *
     * @param zipFile      Input zip file to extract
     * @param outputFolder Output location of files
     * @author Amrik Sadhra, https://www.mkyong.com/
     */
    public static void unzipPresentation(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                if (!newFile.toString().contains(".")) {
                    newFile.mkdir();
                    ze = zis.getNextEntry();
                    continue;
                }

                loggerStatic.info("Unzipping " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Cleanup downloaded zip file by deleting
        new File(zipFile).delete();
    }
}