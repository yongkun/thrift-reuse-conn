#!/bin/sh

. ./profile

# how many lines per RPC, default 1
lines="$1"
if [ $# -eq 0 ]; then
  lines=1
fi

# deprecated
polling="$2"
if [ $# -lt 2 ]; then
  polling=0
fi

server=localhost
port=9091
testFile=$THIS_DIR/resources/client-data.txt

java -cp $CP my.test.JavaClient $server $port $testFile $lines $polling
