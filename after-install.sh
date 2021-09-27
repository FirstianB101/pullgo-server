#!/bin/sh

echo "after-install: pwd=$(pwd)"
cp /home/ec2-user/deployment/pullgo-server-0.0.1-SNAPSHOT.jar /home/ec2-user/pullgo-server.jar
bash /home/ec2-user/deploy.sh
