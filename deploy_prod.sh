#!/bin/bash
# Update Droplet instance with new SocketServer jar
echo "Updating Droplet SocketServer instance with fresh server jar"

# Connect to Droplet so can run fresh SocketServer
echo "-- Connecting via SSH to kill current SocketServer instance"
ssh bscftp@ssh.amriksadhra.com 'sudo killall screen | rm -rf /home/bscftp/CircleBuilds/ | exit'

# Upload Jar file to Droplet instance
curl --ftp-create-dirs -T target/*server-jar-with-dependencies.jar -u bscftp:Combline90+ ftp://ftp.amriksadhra.com/CircleBuilds/

echo "-- Letting Server kill current SocketServer"
sleep 30

echo "-- Connecting via SSH to start new SocketServer instance"
ssh bscftp@ssh.amriksadhra.com 'sudo screen -m -d java -jar /home/bscftp/CircleBuilds/*.jar'
