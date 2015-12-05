import com.aliyun.openservices.ots.OTS;
import com.aliyun.openservices.ots.OTSClient;
import com.aliyun.openservices.ots.OTSException;
import com.aliyun.openservices.ots.model.*;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OtsPipeline implements Pipeline {

    private OTS ots;
    private Settings settings;
    private Client esClient;

    private String otsTableName;
    private String esIndex;
    private String esType;

    public OtsPipeline(OtsConfig otsConfig, EsConfig esConfig) {
        ots = new OTSClient(otsConfig.getEndpoint(), otsConfig.getAccessid(), otsConfig.getAccesskey(), otsConfig.getInstanceName());
        settings = Settings.settingsBuilder()
                .put("cluster.name", esConfig.getClusterName()).build();
        try {
            esClient = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esConfig.getHost()), esConfig.getJavaPort()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.otsTableName = otsConfig.getTableName();
        this.esIndex = esConfig.getIndex();
        this.esType = esConfig.getType();
    }

    private void writeToOts(ResultItems resultItems, String uuid) {
        RowPrimaryKey rowPrimaryKey = new RowPrimaryKey().addPrimaryKeyColumn("PK", PrimaryKeyValue.fromString(uuid));
        RowUpdateChange rowUpdateChange = new RowUpdateChange(otsTableName);
        rowUpdateChange.setPrimaryKey(rowPrimaryKey);
        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            if (entry.getValue() instanceof String) {
                rowUpdateChange.addAttributeColumn(entry.getKey(), ColumnValue.fromString((String) entry.getValue()));
            } else if (entry.getValue() instanceof byte[]) {
                rowUpdateChange.addAttributeColumn(entry.getKey(), ColumnValue.fromBinary((byte[]) entry.getValue()));
            } else {
                throw new RuntimeException(entry.getValue().getClass().getName());
            }
        }

        UpdateRowRequest updateRowRequest = new UpdateRowRequest(rowUpdateChange);
        try {
            ots.updateRow(updateRowRequest);
        } catch (OTSException ex) {
            ex.printStackTrace();
        }
    }

    private void writeToEs(ResultItems resultItems, String uuid) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            map.put(entry.getKey(), entry.getValue().toString());
        }
        IndexResponse response = esClient.prepareIndex(esIndex, esType, uuid)
                .setSource(map)
                .get();
    }

    public void process(ResultItems resultItems, Task task) {
        String uuid = UUID.randomUUID().toString();
        writeToOts(resultItems, uuid);
        writeToEs(resultItems, uuid);
    }
}
