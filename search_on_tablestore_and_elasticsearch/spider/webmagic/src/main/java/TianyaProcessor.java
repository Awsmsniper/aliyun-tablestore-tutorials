import org.ini4j.ConfigParser;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

import java.io.File;
import java.io.IOException;

public class TianyaProcessor implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
    private static int MAX_CONTENT_LENGTH = 16000;

    public void process(Page page) {
        page.addTargetRequests(page.getHtml().links().regex("(http://bbs\\.tianya\\.cn/[a-zA-Z0-9_-]+\\.shtml)").all());
        page.putField("title", page.getHtml().xpath("//title").toString());
        String content = page.getHtml().smartContent().toString();
        page.putField("content", content.substring(0, Math.min(MAX_CONTENT_LENGTH, content.length())));
        page.putField("url", page.getUrl().toString());

        if (content.isEmpty()) {
            page.setSkip(true);
        }
    }

    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws IOException, ConfigParser.NoSectionException, ConfigParser.NoOptionException, ConfigParser.InterpolationException {
        if (args.length != 1) {
            System.out.println("usage: java -jar TianyaProcessor.jar config.ini");
        }
        ConfigParser configParser = new ConfigParser();
        configParser.read(new File(args[0]));

        OtsConfig otsConfig = new OtsConfig(configParser.get("ots", "endpoint"), configParser.get("ots", "accessid"),
                configParser.get("ots", "accesskey"), configParser.get("ots", "instance_name"), configParser.get("ots", "table_name"));
        EsConfig esConfig = new EsConfig(configParser.get("elasticsearch", "cluster_name"), configParser.get("elasticsearch", "host"),
                configParser.getInt("elasticsearch", "java_port"), configParser.get("elasticsearch", "index"),
                configParser.get("elasticsearch", "type"));

        int threadNum = configParser.getInt("spider", "thread_num");

        Spider.create(new TianyaProcessor()).addUrl("http://bbs.tianya.cn/")
                .setScheduler(new FileCacheQueueScheduler("tianyaUrlCache"))
                .addPipeline(new OtsPipeline(otsConfig, esConfig))
                .thread(threadNum).run();
    }

}
