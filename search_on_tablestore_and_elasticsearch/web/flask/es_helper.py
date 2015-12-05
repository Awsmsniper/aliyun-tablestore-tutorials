from elasticsearch import Elasticsearch

class Es_helper:

    def __init__(self, hosts, index, fields):
        self.es = Elasticsearch(hosts)
        self.index = index
        self.fields = fields

    def request(self, body, size):
        es_result = self.es.search(index = self.index, body = body, fields = self.fields, size = size)
        count = es_result["hits"]["total"]
        items = es_result["hits"]["hits"]
        result = {
            "count": count,
            "items": items
        }
        return result

    def search(self, kws, size, offset):
        body = {
            "from": offset,
            "query": {
                "multi_match": {
                    "query": kws,
                    "fields": self.fields
                }
            }
        }
        print body
        return self.request(body, size)

    def adv_search(self, adv_must_kws, adv_must_not_kws, adv_should_kws, adv_should_cnt, adv_fields, size, offset):
        if adv_fields == "all":
            adv_fields = self.fields
        else:
            adv_fields = [ adv_fields ]

        bool_e = {}
        if adv_must_kws != "":
            bool_e["must"] = {
                "multi_match": {
                    "query": adv_must_kws,
                    "fields": adv_fields,
                    "operator": "and"
                }
            }
        if adv_must_not_kws != "":
            bool_e["must_not"] = {
                "multi_match": {
                    "query": adv_must_not_kws,
                    "fields": adv_fields
                }
            }
        if adv_should_kws != "":
            words = adv_should_kws.split()
            bool_e["should"] = []
            for word in words:
                bool_e["should"].append({
                    "multi_match": {
                        "query": word,
                        "fields": adv_fields
                    }
                })
            bool_e["minimum_should_match"] = adv_should_cnt
            
        query = {}
        query["bool"] = bool_e
        body = {}
        body["from"] = offset
        body["query"] = query
        print body
        return self.request(body, size)

