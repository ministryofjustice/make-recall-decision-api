#!/bin/sh
#netstat -l -t | awk '{print $4}' | grep -o '[0-9][0-9][0-9][0-9]*'
ss -lntu | awk '{print $5}' | grep -o ':[0-9][0-9][0-9]*' | grep -o '[0-9][0-9][0-9]*'
#echo "use sudo netstat -ap | grep :<port_number>, followed by kill -9 <PID> to kill a process using a given port
echo "use sudo lsof -n -i :<port_number> | grep LISTEN, followed by sudo kill -9 <pid> to kill a process using given port"
