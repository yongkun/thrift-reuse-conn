THIS_DIR=$(dirname $0)
LIB_DIR=$THIS_DIR/lib/
#IVY_DIR=$LIB_DIR/build/ivy/lib/
# no * after conf/, conf/*, the log4j.properties cannot be loaded
# add * after $LIB_DIR/ , $LIB_DIR/* all jars can be loaded correctly.
CP=.:build:thrift_performance_test.jar:$LIB_DIR/*:conf/
