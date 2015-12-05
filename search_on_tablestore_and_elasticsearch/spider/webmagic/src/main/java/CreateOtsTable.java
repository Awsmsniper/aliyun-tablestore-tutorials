import com.aliyun.openservices.ots.OTS;
import com.aliyun.openservices.ots.OTSClient;
import com.aliyun.openservices.ots.model.CapacityUnit;
import com.aliyun.openservices.ots.model.CreateTableRequest;
import com.aliyun.openservices.ots.model.PrimaryKeyType;
import com.aliyun.openservices.ots.model.TableMeta;
import org.ini4j.ConfigParser;

import java.io.File;
import java.io.IOException;

public class CreateOtsTable {

    public static void main(String[] args) throws IOException, ConfigParser.NoSectionException, ConfigParser.NoOptionException, ConfigParser.InterpolationException {
        if (args.length != 1) {
            System.out.println("usage: java -jar CreateOtsTable.jar config.ini");
        }
        ConfigParser configParser = new ConfigParser();
        configParser.read(new File(args[0]));

        OTS ots = new OTSClient(configParser.get("ots", "endpoint"), configParser.get("ots", "accessid"),
            configParser.get("ots", "accesskey"), configParser.get("ots", "instance_name"));

        String otsTableName = configParser.get("ots", "table_name");

        TableMeta tableMeta = new TableMeta(otsTableName);
        tableMeta.addPrimaryKeyColumn("PK", PrimaryKeyType.STRING);
        CreateTableRequest createTableRequest = new CreateTableRequest(tableMeta);
        createTableRequest.setReservedThroughput(new CapacityUnit(configParser.getInt("ots", "read_cu"), configParser.getInt("ots", "write_cu")));
        ots.createTable(createTableRequest);
        ots.shutdown();
    }
}
