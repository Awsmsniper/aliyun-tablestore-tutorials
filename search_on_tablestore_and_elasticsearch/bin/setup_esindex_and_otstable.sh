#!/usr/bin/env bash

set -o nounset
set -o errexit

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -jar spider/webmagic/CreateOtsTable.jar $DIR/../config.ini || print ""

index=`grep 'index' $DIR/../config.ini | cut -d '=' -f 2`
type=`grep 'type' $DIR/../config.ini | cut -d '=' -f 2`

curl -XPUT http://localhost:9200/$index

curl -XPOST http://localhost:9200/$index/$type/_mapping -d'
{
    '$type': {
        "_all": {
            "analyzer": "ik_max_word",
            "search_analyzer": "ik_max_word",
            "term_vector": "no"
        },
        "properties": {
            "content": {
                "type": "string",
                "term_vector": "with_positions_offsets",
                "analyzer": "ik_max_word",
                "search_analyzer": "ik_max_word",
                "include_in_all": "true",
                "boost": 8,
                "store": "false"
            },
            "title": {
                "type": "string",
                "term_vector": "with_positions_offsets",
                "analyzer": "ik_max_word",
                "search_analyzer": "ik_max_word",
                "include_in_all": "true",
                "boost": 8,
                "store": "true"
            },
            "abstract": {
                "type": "string",
                "term_vector": "with_positions_offsets",
                "analyzer": "ik_max_word",
                "search_analyzer": "ik_max_word",
                "include_in_all": "true",
                "boost": 8,
                "store": "true"
            }, 
            "url": {
                "type": "string",
                "store": "true"
            }
        },
        "_source": {
            "enabled": false
        }
    }
}'
