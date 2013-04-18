#!/bin/sh

. ./profile

# how many lines per RPC, default 1
lines="$1"
if [ $# -eq 0 ]; then
  lines=1
fi

port=9091
testFile=$THIS_DIR/resources/server-data.txt

java -cp $CP my.test.JavaServer $port $testFile $lines
