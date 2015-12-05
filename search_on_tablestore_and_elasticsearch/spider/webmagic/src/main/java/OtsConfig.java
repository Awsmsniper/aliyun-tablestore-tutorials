public class OtsConfig {

    private String endpoint;
    private String accessid;
    private String accesskey;
    private String instanceName;

    private String tableName;

    public OtsConfig(String endpoint, String accessid, String accesskey, String instanceName, String tableName) {
        this.endpoint = endpoint;
        this.accessid = accessid;
        this.accesskey = accesskey;
        this.instanceName = instanceName;
        this.tableName = tableName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessid() {
        return accessid;
    }

    public void setAccessid(String accessid) {
        this.accessid = accessid;
    }

    public String getAccesskey() {
        return accesskey;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
