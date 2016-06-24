## TableStore（原OTS） + Elasticsearch

#### TableStore是什么
	
1. TableStore是一个构建在阿里云飞天分布式系统上的Nosql数据库服务，熟悉阿里云的同学肯定听说过飞天5K，飞天是一个可以管理5000台机器的分布式系统，TableStore作为构建在其上的一个Nosql数据库，可以承载海量（单表几百TB）的数据存储，同时数据有三份拷贝，数据安全性有极高的保证。

2. TableStore的数据是以行进行组织的，每行包含多个主键列和多个属性列，主键列的列名和类型是固定的，TableStore根据主键列的大小对行进行排序，属性列的列名和类型不是固定的，行之间可以不同。用户查询的时候，给出一个完整的主键，可用于查询一行，给出一个完整的主键范围，可以查询一个范围内的行。所以TableStore可以认为是Nosql中的列族数据库，与HBase类似。TableStore可以作为键值数据库使用，也适用于存储结构化、半结构化的数据。

3. TableStore的性能如何？单行查询延迟在几个毫秒级别，范围查询可以打满网卡，性能绝对不差。TableStore会自动对表进行数据分片和负载均衡，可以支持10万级别以上的QPS。特别提一点，读写能力最大5000只是针对初始使用TableStore的用户的建表限制，当您有更高的需求时，联系我们就可以很方便提升上去。

4. 服务化：即开即用、无需运维，按量付费，认证、授权方式多样，开发团队在线支持等。

#### Elasticsearch是什么

1. Elasticsearch是一个实时分布式搜索和分析引擎，它的索引和搜索功能非常强大，所以使用场景很丰富。比如Github使用Elasticsearch检索上千亿行的代码，维基百科和StackOverFlow也都使用了Elasticsearch提供搜索服务。Elasticsearch还非常适合日志处理的场景，有著名的ELKStack日志处理方案，ELK是指Elasticsearch、Logstash、Kibana，整套方案包括了日志收集、聚合、多维度查询、可视化显示等。

2. Elasticsearch基于Lucene，Lucene是一个非常复杂的搜索引擎库，而Elasticsearch提供的是更易用的一体化的分布式实时搜索应用。不需要特别专业的搜索知识即可使用Elasticsearch构建一套搜索服务。

#### TableStore + ES的场景分析：

如上所述，TableStore是一个Nosql数据库，也是一个稳定安全的大数据存储服务。围绕TableStore可以结合许多其他软件，比如Elasticsearch、Spark等。TableStore结合Elasticsearch具有很好的优势互补作用：
	
1. 简单来说，TableStore是一款Nosql数据库，而Elasticsearch是一个搜索服务，在稳定性、可靠性、读写性能上不及TableStore。

2. Elasticsearch查询、搜索功能丰富，可以弥补TableStore查询方式的不足。

3. TableStore保存原始数据，同步到Elasticsearch中建立索引，可以提供更高的数据安全性，方便对数据的额外处理。

4. 原始数据在TableStore里，Elasticsearch中就不需要保存原始数据，只需要保存TableStore对应行的主键值，在需要访问原始数据时，直接访问TableStore获取，这样的架构可靠性和性能都更好。“主存储系统（例如TableStore、HBase）＋ Elasticsearch“是业界使用Elasticsearch的常见架构。

	几种结合场景：
	
	1. Elasticsearch作为TableStore的扩充，使表中的数据可被多条件查询或全文检索，相当于为TableStore的表增加了搜索功能。
	
		比如，客户使用TableStore作为在线数据库，读写性能和查询方式基本可以满足业务需要，但是又有一些特殊的多条件查询（或搜索）需求无法满足，而这种查询的次数相对较少、实时性要求也不高，那么就可以将数据从TableStore同步到Elasticsearch里，使用Elasticsearch来支持这些查询。
	
	2. TableStore存储原始数据，导入Elasticsearch中进行索引，Elasticsearch可以不保存原始数据，只需要保存对应TableStore的映射和一些信息摘要。这种场景适合于本身就使用Elasticsearch的用户，但是用户缺少一个合适的存储产品来弥补Elasticsearch的一些不足和扩展存储能力。如果用户本身使用HBase和Elasticsearch，也可以考虑将HBase迁移到TableStore来减少运维负担，以及节约成本。
	
	3. 使用TableStore、OSS（对象存储服务）存储结构化数据以及非结构化数据，Elasticsearch建立索引，用于搜索。在这个场景中，TableStore存储结构化的信息以及非结构化信息（如图片、视频等）在OSS中的地址，将需要检索的内容和TableStore的主键值导入到Elasticsearch中建立索引。每次通过Elasticsearch搜索后，通过结果中的TableStore主键值就可以获取对应行的数据以及OSS中文件的url等。
		
### 实战：利用TableStore、ES构建搜索引擎

#### 目标：
	
爬取特定网站的页面，将页面内容保存在TableStore中，在Elasticsearch中建立网站的索引，通过web页面搜索框的形式，对爬取的页面进行搜索。

#### 以天涯论坛为例，一套简单的方案：


爬取天涯论坛的帖子页面，提取的信息包括Url，标题，帖子内容。数据首先存入TableStore中，主键为随机的一个UUID，同时将Url、标题、帖子内容、UUID导入Elasticsearch中建立索引，Elasticsearch只保存Url、标题和UUID，不保存帖子内容。同时构建一个网页，通过搜索框输入关键词，在Elasticsearch中搜索获取结果，

1. 整个方案涉及四个模块：TableStore、Elasticsearch、Spider、Web。

2. TableStore不需要搭建，只需要开通服务、建表即可。我们使用TableStore来存取页面数据，每个页面的数据保存为TableStore的一行，使用UpdateRow写入，使用GetRow读出。

3. Elasticsearch需要在ECS中搭建起来，搭建Elasticsearch非常简单，只需要下载下来运行启动脚本即可。与TableStore建表类似，在搭建完成Elasticsearch之后，需要在Elasticsearch中建立索引（Index），并需要在索引上建立映射（Mapping）。

4. Spider具体爬取页面，我们选取了一个简单易用的开源爬虫软件，WebMagic。使用WebMagic的接口，我们可以方便的把爬取的页面数据存入TableStore和Elasticsearch。

5. Web指网页搜索的前后端。出于开发效率的考虑，我们选择一些接口简单易用的工具进行开发，后端使用一个Python的web框架flask，前端使用vue.js。


#### 快速部署与体验

我们已经实现了上述方案，并且提供了源码、部署脚本和使用说明。
	
1. 项目代码解压后结构如下所示, 请先阅读README.md文件:
		![image](https://gist.githubusercontent.com/whybert/919bbe685f246b00308f/raw/d2be22a2799d7be3f43ef76f5d1e31565839ab0b/demo1_1.png)
2. 执行以下命令生成配置文件：
	
			cp config.ini.template config.ini 

3. 修改配置文件，填入endpoint, accessid, accesskey, instancename等信息，需要在阿里云官网开通表格存储服务（TableStore，原OTS），然后创建实例。
	
		配置文件内容如下：
	
			[ots]
            endpoint=http://xxx:xxx   #改成自己的endpoint
            accessid=xxx			   #改成自己的accessid
            accesskey=xxx             #改成自己的accesskey
            instance_name=xxx         #改成自己的instanceName
            
            #以下参数可以用默认值。
            table_name=elasticsearch_data  #TableStore的表名
            read_cu=0              #表的预留读能力单元，同时与计费相关
            write_cu=0             #表的预留写能力单元，同时与计费相关
    
            [elasticsearch]
            cluster_name=ots_elasticsearch
            host=localhost
            port=9200
            java_port=9300
            index=es_idx
            type=web
            fields=["title", "url", "content"]
    
            [spider]
            thread_num=10
			
4. 安装相关软件：
		
			需要预先准备好：
				Java 1.7
				Python 2.7
				pip
				curl
			
			执行：
			./bin/deploy.sh	
			
5. 初始化：
	
			启动Elasticsearch：
			./bin/start_elasticsearch.sh
			这里只会启动一个ES节点。	

			初始化TableStore表和Elasticsearch索引：
			./bin/setup_esindex_and_otstable.sh
	
6. 运行爬虫：
		
			./bin/start_spider.sh
			
7. 运行搜索Web界面
	
			./bin/start_web.sh
			
			默认会绑定0.0.0.0:5050端口。
	
8. 访问：
			
			http://x.x.x.x:5050
			
	![image](https://gist.githubusercontent.com/whybert/919bbe685f246b00308f/raw/d2be22a2799d7be3f43ef76f5d1e31565839ab0b/demo1_2.png)
			
	

#### 具体实现

以下摘取一些代码片段，从中可以看到各个部分简要的实现方式。

* OTS

	1. 建表
	
			void createTable() {
				OTS ots = new OTSClient("http://your_endpoint", "your_accessid", "your_accesskey", "your_instancename");
                String otsTableName = "your_tablename";
                TableMeta tableMeta = new TableMeta(otsTableName);
                tableMeta.addPrimaryKeyColumn("PK", PrimaryKeyType.STRING);
                CreateTableRequest createTableRequest = new CreateTableRequest(tableMeta);
                createTableRequest.setReservedThroughput(new CapacityUnit(1000, 1000)); //OTS按照读写预留能力收费，这里可以根据实际情况调整。
                ots.createTable(createTableRequest);
                ots.shutdown();	
            }	
            也可以通过控制台建表
            
			
	2. UpdateRow
			
			void updateRow(OTS ots, String uuid, String url, String title, String content) {
			  	String otsTableName = "tianya_web_data";
                RowPrimaryKey rowPrimaryKey = new RowPrimaryKey().addPrimaryKeyColumn("PK", PrimaryKeyValue.fromString(uuid));
                RowUpdateChange rowUpdateChange = new RowUpdateChange(otsTableName);
                rowUpdateChange.setPrimaryKey(rowPrimaryKey);
        
                rowUpdateChange.addAttributeColumn("url", ColumnValue.fromString(url));
                rowUpdateChange.addAttributeColumn("title", ColumnValue.fromString(title));
                rowUpdateChange.addAttributeColumn("content", ColumnValue.fromString(content));
        
                UpdateRowRequest updateRowRequest = new UpdateRowRequest(rowUpdateChange);
                ots.updateRow(updateRowRequest);
            }
	
		
	3. GetRow
			
			Java:
			Row getRow(OTS ots, String uuid) {
                String otsTableName = "tianya_web_data";
                RowPrimaryKey rowPrimaryKey = new RowPrimaryKey().addPrimaryKeyColumn("PK", PrimaryKeyValue.fromString(uuid));
                SingleRowQueryCriteria rowQueryCriteria = new SingleRowQueryCriteria(otsTableName);
                rowQueryCriteria.setPrimaryKey(rowPrimaryKey);
        
                GetRowRequest getRowRequest = new GetRowRequest(rowQueryCriteria);
                GetRowResult getRowResult = ots.getRow(getRowRequest);
                Row row = getRowResult.getRow();
                System.out.println(row);
                return row;
            }
	
			Python:
			from ots2 import OTSClient
			ots = OTSClient(endpoint, accessid, accesskey, instance_name)
			primary_key = {"PK" : uuid}
			consumed, primary_key_columns, attribute_columns = ots.get_row(table_name, primary_key)
			

* Elasticsearch

	1. 搭建
		
		下载elasticsearch2.0.0版本，解压，执行：
		
			./elasticsearch-2.0.0/bin/elasticsearch --cluster.name ots_elasticsearch --node.name es1 --network.host  0.0.0.0
			该命令启动了一个集群名为ots_elasticsearch, 节点名为es1的节点。
		
	2. 分词插件
	
		elasticsearch默认的中文分词效果不好，所以要给elasticsearch安装一个中文分词插件，这里使用elasticsearch-analysis-ik插件：
				
			https://github.com/medcl/elasticsearch-analysis-ik
			安装方式也参照上述链接。
		
	3. 建立Index
	    
			创建一个名为tianya的Index，一个Index相当于一个数据库的概念：
			curl -XPUT http://localhost:9200/tianya
			
	4. 建立Mapping
	
			在Index上建立映射（Mapping），映射可以配置字段的类型，分词方式等。
			curl -XPOST http://localhost:9200/tianya/web/_mapping -d'
            {
                "web": {
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
		        
	5. 搜索
		
		 	一个简单的全文查询示例：
		 	
			es = Elasticsearch()
			index = "tianya"
			body = { 
            	"from": 0,
            	"query": {
                	"multi_match": {
                    	"query": kws,
                    	"fields": ["title", "content"]
                	}   
            	}
        	}
        	es_result = es.search(index = index, body = body, size = 10)

	
* Spider

    1. 使用WebMagic
    		
    		http://webmagic.io/
    		
    2. 定制天涯爬虫
    		
    		使用webmagic，定制一个天涯论坛的Processor：
    		public class TianyaProcessor implements PageProcessor {
                private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
            
                public void process(Page page) {
                    page.addTargetRequests(page.getHtml().links().regex("(http://bbs\\.tianya\\.cn/[a-zA-Z0-9_-]+\\.shtml)").all());
                    page.putField("title", page.getHtml().xpath("//title").toString());
                    String content = page.getHtml().smartContent().toString();
                    page.putField("content", content)));
                    page.putField("url", page.getUrl().toString());
            
                    if (content.isEmpty()) {
                        page.setSkip(true);
                    }
                }
            }
    		
    3. 写入TableStore、ES
    		
    		写入TableStore的方法如上文所示，可以通过UpdateRow接口写入。
    		写入Elasticsearch的方法：
    		
    		void writeToElasticsearch(Client esClient, String esIndex, String esType, String uuid, String url, String title, String content) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("url", url);
                map.put("title", title);
                map.put("content", content);
                IndexResponse response = esClient.prepareIndex(esIndex, esType, uuid)
                        .setSource(map)
                        .get();
            }
    		

* Web

	1. 服务端Flask
			
			http://flask.pocoo.org/
			http://docs.jinkan.org/docs/flask/
			
	2. 前端Vue.js
		
			http://cn.vuejs.org/
			https://github.com/vuejs/vue-resource
