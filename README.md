thrift-reuse-conn
=================

A simple example on how to reuse the thrift connection to transmit data bi-directionally, inspired by http://joelpm.com/2009/04/03/thrift-bidirectional-async-rpc.html

Thrift is a cross-language RPC framework. http://thrift.apache.org

In this example, Client connect to server and send data, Server will send acknowledgement data via the same connection.

How to:

1. Download the source;
2. type 'ant', you need to have thrift installed;

Start Server:

  ./JavaServer.sh


Open another terminal and start Client:

  ./JavaClient.sh


