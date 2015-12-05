#!/usr/bin/env bash

set -o nounset
set -o errexit

CUR_PATH=`dirname $0`
BASE_DIR=`cd "$CUR_PATH" && pwd`/..
cd $BASE_DIR/elasticsearch

if [ ! -d "elasticsearch-2.0.0" ]; then
    unzip elasticsearch-2.0.0.zip
    mkdir elasticsearch-2.0.0/plugins
fi

if [ ! -d "elasticsearch-2.0.0/plugins/elasticsearch-analysis-ik-1.5.0" ]; then
    unzip elasticsearch-analysis-ik-1.5.0.zip -d elasticsearch-2.0.0/plugins/elasticsearch-analysis-ik-1.5.0
    cp -rf ik elasticsearch-2.0.0/config/
fi

cd $BASE_DIR/web/flask

sudo pip install virtualenv
virtualenv venv
./venv/bin/pip install Flask -i http://pypi.douban.com/simple --trusted-host pypi.douban.com
./venv/bin/pip install Elasticsearch -i http://pypi.douban.com/simple --trusted-host pypi.douban.com
./venv/bin/pip install gunicorn -i http://pypi.douban.com/simple --trusted-host pypi.douban.com

cd ots/python/pymodules

cd distribute-0.7.3
../../../../venv/bin/python setup.py install
cd ..

cd protobuf-2.5.0
../../../../venv/bin/python setup.py install
cd ..

cd setuptools-1.3.2
../../../../venv/bin/python setup.py install
cd ..

cd urllib3-1.11
../../../../venv/bin/python setup.py install
cd ..

cd ..
../../venv/bin/python setup.py install

