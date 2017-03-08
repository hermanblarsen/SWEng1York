#!/bin/bash
# Send SureFire Report to my Server for syncing with GDrive (Kacper QA)

   echo "Deploying Maven Surefire Test Report to remote server"
   mvn surefire-report:report-only
   curl --ftp-create-dirs -T target/site/surefire-report.html > surefire-report.html -u bscftp:Combline90+ ftp://ftp.amriksadhra.com/TravisBuilds/
