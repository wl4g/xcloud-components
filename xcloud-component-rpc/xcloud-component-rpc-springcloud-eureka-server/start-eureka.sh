#!/bin/bash

EXEC_FILE="eureka-server-master-bin.jar"
JVM_OPTS="--server.tomcat.basedir=/mnt/disk1/eureka/"
if [ "$1" == "--local" ]; then
  nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=local --logging.file.name=/mnt/disk1/log/eureka/eureka.log 2>&1 >/dev/null &
  echo "Started eureka(local) successfully!"
elif [ "$1" == "--cluster" ]; then
  echo "Starting for peer1..."
  nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=ha,peer1 --logging.file.name=/mnt/disk1/log/eureka/eureka-ha,peer1.log 2>&1 >/dev/null &
  sleep 2

  echo "Starting for peer2..."
  nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=ha,peer2 --logging.file.name=/mnt/disk1/log/eureka/eureka-ha,peer2.log 2>&1 >/dev/null &
  sleep 2

  echo "Starting for peer3..."
  nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=ha,peer3 --logging.file.name=/mnt/disk1/log/eureka/eureka-ha,peer3.log 2>&1 >/dev/null &

  echo "Starting eureka(peer1,peer2,peer3) completed!"
else
  echo "Please specify eureka startup mode! For example: --local | --cluster"
fi

exit 0
