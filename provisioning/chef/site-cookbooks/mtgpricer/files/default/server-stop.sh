#!/bin/bash

PID_PATH=/tmp/mtgpricer_webapp.pid
if [ -f $PID_PATH ]; then
    PID=$(cat $PID_PATH);
    echo "Stopping mtgpricer_webapp"
    kill $PID;
    echo "Stopped mtgpricer_webapp"
    rm $PID_PATH
fi