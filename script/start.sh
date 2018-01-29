#!/bin/bash
#调用方式  ./start.sh canal_data_client-dev.jar
JAVA_HOME=/usr/java

chmod +x ${1}
pid=`ps -ef|grep ${1}|grep java |grep -v grep |awk '{print $2}'`
echo $pid
[ -n "$pid" ] && kill -9 $pid
sleep 2
nohup >> nohup.out  $JAVA_HOME/bin/java -jar ${1} 2>&1 &

