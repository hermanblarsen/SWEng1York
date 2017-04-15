#!/bin/bash
# Send SureFire Report to Google Drive for syncing with GDrive (Kacper QA)

echo "Test Report Generation and Google Drive Upload"
echo "-- Generating Maven Surefire test report"
today=`date '+%Y_%m_%d__%H_%M_%S'`;
filename="$today-$CIRCLE_SHA1-test-report.html"

# Generate reports and rename to contain date, time, commit SHA1
mvn surefire-report:report-only
mv target/site/surefire-report.html target/site/$filename

# Make gdrive executable
chmod a+x ./bin/gdrive

echo "-- Uploading HTML report to google drive"
 ./bin/gdrive --refresh-token "$GDRIVE_REFRESH_TOKEN" upload --parent "$GDRIVE_DIR" target/site/$filename

# Make Artifact collection easy on Circle by moving to some Dirs
mkdir -p "$CIRCLE_ARTIFACTS/Test Reports/junit/"
mkdir -p "$CIRCLE_ARTIFACTS/JAR Files/"

echo "-- Moving Build Artifacts"
find . -type f -regex ".*/target/surefire-reports/.*xml" -exec cp {} "$CIRCLE_ARTIFACTS/Test Reports/junit/" \;
find . -type f -regex ".*/target/site/.*html" -exec cp {} "$CIRCLE_ARTIFACTS/Test Reports/junit/" \;
find . -type f -regex ".*/target/.*jar" -exec cp {} "$CIRCLE_ARTIFACTS/JAR Files/" \;

# Update Droplet instance with new SocketServer jar
echo "Updating Droplet SocketServer instance with fresh server jar"

# Connect to Droplet so can run fresh SocketServer
echo "-- Connecting via SSH to kill current SocketServer instance"
ssh bscftp@ssh.amriksadhra.com 'killall screen | rm -rf /home/bscftp/CircleBuilds/ | exit'

# Upload Jar file to Droplet instance
curl --ftp-create-dirs -T target/*server-jar-with-dependencies.jar -u bscftp:Combline90+ ftp://ftp.amriksadhra.com/CircleBuilds/

echo "-- Connecting via SSH to start new SocketServer instance"
ssh bscftp@ssh.amriksadhra.com 'screen -m -d java -jar /home/bscftp/CircleBuilds/*.jar'

