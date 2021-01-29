#!/bin/bash

EXEC_FILE="eureka-server-master-bin.jar"
BASE_DIR="/mnt/disk1/eureka/"

function start() {
  JVM_OPTS="--server.tomcat.basedir=${BASE_DIR}"
  if [ "$1" == "--standalone" ]; then
    nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=standalone >/dev/null 2>&1 &
    echo "Started eureka(standalone) successfully!"
  elif [ "$1" == "--cluster" ]; then
    echo "Starting for peer1..."
    nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=ha,peer1 >/dev/null 2>&1 &

    echo "Starting for peer2..."
    nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=ha,peer2 >/dev/null 2>&1 &

    echo "Starting for peer3..."
    nohup java -jar ${EXEC_FILE} ${JVM_OPTS} --spring.profiles.active=ha,peer3 >/dev/null 2>&1 &

    echo "Starting eureka(peer1,peer2,peer3) completed!"
  else
    echo "Please specify eureka startup mode! For example: --standalone | --cluster"
  fi
  echo "Please goto dir: '${BASE_DIR}../log/eureka/' for the logs."
}

function stop(){
  PIDS=$(ps -ef|grep java|grep -v grep|grep ${BASE_DIR})
  if [ ! -n "$PIDS" ]; then
    echo "No running eureka server!"
    exit 0
  fi
  echo "Stopping eureka server all nodes ..."
  ps -ef|grep java|grep -v grep|grep ${BASE_DIR}|cut -c 9-15|xargs kill -s TERM
  echo "Stopped eureka server successfully!"
}

CMD=$1
ARG1=$2
if [ "$CMD" == "--start" ]; then
  start "$ARG1"
elif [ "$CMD" == "--stop" ]; then
  stop "$ARG1"
else
  echo "Bad command args! For example: --start --standalone or --start --cluster or --stop"
fi

exit 0
