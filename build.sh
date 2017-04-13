#!/bin/bash
# Send SureFire Report to my Server for syncing with GDrive (Kacper QA)

   echo "Deploying Maven Surefire Test Report to remote server"
   today=`date '+%Y_%m_%d__%H_%M_%S'`;
   filename="$today-$TRAVIS_COMMIT-test-report.html"

   mvn surefire-report:report-only
   mv target/site/surefire-report.html target/site/$filename
