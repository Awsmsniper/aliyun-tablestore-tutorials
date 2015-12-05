from ots2 import OTSClient

class Ots_helper:

    def __init__(self, endpoint, accessid, accesskey, instance_name):
        self.ots = OTSClient(endpoint, accessid, accesskey, instance_name)

    def get(self, table_name, primary_key, columns_to_get):
        return self.ots.get_row(table_name, primary_key, columns_to_get)

