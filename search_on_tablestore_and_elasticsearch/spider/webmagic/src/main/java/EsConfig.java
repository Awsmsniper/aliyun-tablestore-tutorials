public class EsConfig {

    private String clusterName;
    private String host;
    private int javaPort;
    private String index;
    private String type;

    public EsConfig(String clusterName, String host, int javaPort, String index, String type) {
        this.clusterName = clusterName;
        this.host = host;
        this.javaPort = javaPort;
        this.index = index;
        this.type = type;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getJavaPort() {
        return javaPort;
    }

    public void setJavaPort(int javaPort) {
        this.javaPort = javaPort;
    }
}
