#!/usr/bin/env bash

set -o nounset
set -o errexit

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -jar spider/webmagic/TianyaProcessor.jar $DIR/../config.ini
