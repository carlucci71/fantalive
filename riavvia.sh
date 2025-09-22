#!/bin/bash
java_pid=$(ps aux | grep fantalive | grep MainClass | awk '!/awk/ {print $2}')

if [ -z "$java_pid" ]; then
    echo "Nessun processo Java trovato con il nome: $process_name"
else
    echo "Terminazione del processo Java con PID: $java_pid"
    kill -9 "$java_pid"
fi
#git pull
#current_time=$(date +"%H:%M:%S")
#echo "KILLATO ALLE: $current_time" >>  /home/daniele/loggariavvia.txt 
cd /home/daniele/github/fantalive
#pwd >> /home/daniele/loggariavvia.txt
sh ./lancia.sh
