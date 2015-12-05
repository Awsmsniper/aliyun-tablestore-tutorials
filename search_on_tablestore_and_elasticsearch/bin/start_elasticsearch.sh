#!/usr/bin/env bash

set -o nounset
set -o errexit

CUR_PATH=`dirname $0`
BASE_DIR=`cd "$CUR_PATH" && pwd`/..
cd $BASE_DIR/elasticsearch

./elasticsearch-2.0.0/bin/elasticsearch --cluster.name ots_elasticsearch --node.name es1 --network.host  0.0.0.0

