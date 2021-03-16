#!/bin/sh

echo "after-install: pwd=$(pwd)"
cp /deployment-root/$DEPLOYMENT_GROUP_ID/$DEPLOYMENT_ID/deployment-archive/pullgo-server-*.jar /home/ec2-user/pullgo-server.jar
bash /home/ec2-user/deploy.sh
