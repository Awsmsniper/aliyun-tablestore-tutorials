from flask import Flask, request, render_template
from es_helper import Es_helper
from ots_helper import Ots_helper

import sys
import json
import ConfigParser

####### config ######
cf = ConfigParser.ConfigParser()
cf.read("../../config.ini")
    
ots_endpoint = cf.get("ots", "endpoint")
ots_accessid = cf.get("ots", "accessid")
ots_accesskey = cf.get("ots", "accesskey")
ots_instance_name = cf.get("ots", "instance_name")
es_host = cf.get("elasticsearch", "host")
es_port = int(cf.get("elasticsearch", "port"))
es_index = cf.get("elasticsearch", "index")
es_fields = json.loads(cf.get("elasticsearch", "fields"))

es_helper = Es_helper(hosts=[{"host":es_host, "port": es_port}], index=es_index, fields=es_fields)
ots_helper = Ots_helper(ots_endpoint, ots_accessid, ots_accesskey, ots_instance_name)
ots_table_name = cf.get("ots", "table_name")

app = Flask(__name__)

def byteify(input):
    if isinstance(input, dict):
        return {byteify(key):byteify(value) for key,value in input.iteritems()}    
    elif isinstance(input, list):
        return [byteify(element) for element in input]
    elif isinstance(input, unicode):
        return input.encode('utf-8')
    else:
        return input

@app.route('/helloworld')
def hello_world():
    return 'Hello World!'

@app.route('/')
def index():
    return app.send_static_file('index.html')

@app.route('/search')
def search():
    print "search..."
    kws = request.args.get('kws', "", type=unicode)
    size = request.args.get('size', 0, type=int)
    offset = request.args.get('offset', 0, type=int)
    search_type = request.args.get('search_type', "normal_search", type=unicode)
    if search_type == "normal_search":
        kws = request.args.get('kws', "", type=unicode)
        print kws, size, offset
        return json.dumps(byteify(es_helper.search(kws, size, offset)), ensure_ascii=False)
    else:
        adv_must_kws = request.args.get('adv_must_kws', "", type=unicode)
        adv_must_not_kws = request.args.get('adv_must_not_kws', "", type=unicode)
        adv_should_kws = request.args.get('adv_should_kws', "", type=unicode)
        adv_should_cnt = request.args.get('adv_should_cnt', 1, type=int)
        adv_fields = request.args.get('adv_fields', "", type=unicode)
        print adv_must_kws, adv_must_not_kws, adv_should_kws, adv_should_cnt, adv_fields, size, offset
        return json.dumps(byteify(es_helper.adv_search(adv_must_kws, adv_must_not_kws, adv_should_kws, adv_should_cnt, adv_fields, size, offset)), ensure_ascii=False)

@app.route('/data')
def ots_data():
    pk = request.args.get('key', "", type=str)
    content = ots_helper.get(ots_table_name, {"PK": pk}, ["content"])[2]
    res = byteify(content)
    return json.dumps(res, ensure_ascii=False)

