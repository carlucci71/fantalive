#!/bin/bash
java_pid=$(ps aux | grep fantalive | grep MainClass | awk '!/awk/ {print $2}')

if [ -z "$java_pid" ]; then
    echo "Nessun processo Java trovato con il nome: $process_name"
else
    echo "Terminazione del processo Java con PID: $java_pid"
    kill "$java_pid"
fi
git pull
./lancia.sh
