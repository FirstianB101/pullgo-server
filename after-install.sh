#!/bin/sh

cd "deployment-root/${DEPLOYMENT_GROUP_ID}/${DEPLOYMENT_ID}/deployment-archive/" || exit
echo "after-install: pwd=$(pwd)"
cp pullgo-server-0.0.1-SNAPSHOT.jar /home/ec2-user/pullgo-server.jar
bash /home/ec2-user/deploy.sh
