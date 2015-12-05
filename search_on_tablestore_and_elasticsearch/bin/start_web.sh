#!/usr/bin/env bash

set -o errexit

CUR_PATH=`dirname $0`
BASE_DIR=`cd "$CUR_PATH" && pwd`/..
cd $BASE_DIR/web/flask

. ./venv/bin/activate

gunicorn -c gun.conf main:app
