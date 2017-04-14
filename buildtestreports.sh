#!/bin/bash
# Send SureFire Report to Google Drive for syncing with GDrive (Kacper QA)

echo "Generating Maven Surefire test report"
today=`date '+%Y_%m_%d__%H_%M_%S'`;
filename="$today-$CIRCLE_SHA1-test-report.html"

mvn surefire-report:report-only
mv target/site/surefire-report.html target/site/$filename


echo "Uploading HTML report to google drive"
 ./bin/gdrive --refresh-token "$GDRIVE_REFRESH_TOKEN" upload --parent "$GDRIVE_DIR" target/site/$filename

