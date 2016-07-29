package com.company.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.company.model.DmMobile;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class Tool_Mongo {

	private static final Logger logger = Logger.getLogger(Tool_Mongo.class);
	
	private static Gson gson = new Gson();
    private static MongoClient mongoClient = null;
	
	/**
	 * 加载args配置文件
	 * 
	 */
    private static final Properties mongodb_properties = new Properties();
    
    static {
    	try (InputStream resourceAsStream = Tool_Mongo.class.getResourceAsStream("/conf/mongodb.properties");) {
    		mongodb_properties.load(resourceAsStream);
    	} catch (IOException e) {}
    	
    	logger.info("#读取  mongodb_raw_data 配置文件");
    }
    
    public static final String mongodb_address = mongodb_properties.getProperty("mongo.adress");
    public static final String mongodb_db = mongodb_properties.getProperty("mongo.db");
    
    public static final String mongo_collection_dm_mobile = mongodb_properties.getProperty("mongo.dm.mobile.collection");
    
    public static void init_mongodb_pool() throws IOException {
    	logger.info("mongo address: " + mongodb_address);
		logger.info("mongo db: " + mongodb_db);
		
		get_mongo_db();
	}
    
    /**
     * 获取db
     * @return
     */
	@SuppressWarnings("deprecation")
	public static DB get_mongo_db() {
		if (mongoClient == null) {
			logger.info("#创建 mongodb 连接池");
			
			try {
				
				List<ServerAddress> mongoAddresses = new ArrayList<ServerAddress>();
				for (String address : mongodb_address.split(",")) {
					mongoAddresses.add(new ServerAddress(address.split(":")[0].trim(), Integer.parseInt(address.split(":")[1].trim())));
				}
				
				MongoClientOptions options = null;
				if (mongoAddresses.size() > 1) { // 副本集
					options = MongoClientOptions.builder().readPreference(ReadPreference.secondaryPreferred()).writeConcern(WriteConcern.REPLICA_ACKNOWLEDGED).build();
				} else { // 非副本集，不支持REPLICA_ACKNOWLEDGED
					options = MongoClientOptions.builder().readPreference(ReadPreference.secondaryPreferred()).writeConcern(WriteConcern.ACKNOWLEDGED).build();
				}
				mongoClient = new MongoClient(mongoAddresses, options);
				
			} catch (Exception e) {
				logger.warn("获取 mongodb db异常", e);
			}
		}
		//return mongoClient.getDatabase(mongodb_db); // 3.0
		return mongoClient.getDB(mongodb_db); // 2.6
	}
    
    /**
	 * 获取 dm_mobile collection
	 * 
	 * @return 
	 */
	public static DBCollection get_mongo_collection_dm_mobile() {
		return get_mongo_db().getCollection(mongo_collection_dm_mobile);
	}
	
	/**
	 * 根据Key获取DmMobile
	 * @param key
	 * @return
	 */
	public static DmMobile getDmMobileByKey(String key) {
		if (Strings.isNullOrEmpty(key) || !key.matches("\\d{7}")) {
			return null;
		}
		
		DmMobile mobile = null;
		
		DBCursor cursor = get_mongo_collection_dm_mobile().find(new BasicDBObject("Key", key));
		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			
			mobile = gson.fromJson(gson.toJson(obj), DmMobile.class);
			mobile.set_id(obj.get("_id"));
		}
		
		return mobile;
	}
	
	/**
	 * 根据keys获取DmMobile
	 * @param keys
	 * @return
	 */
	public static Map<String, DmMobile> getDmMobileByKey(List<String> keys) {
		if (keys == null || keys.size() == 0) {
			return null;
		}
		
		Map<String, DmMobile> mobiles = new HashMap<String, DmMobile>();
		
		BasicDBList keyList = new BasicDBList();
		keys.stream().forEach(key -> {
			keyList.add(key);
		});
		DBCursor cursor = get_mongo_collection_dm_mobile().find(new BasicDBObject("Key", new BasicDBObject("$in", keyList)));
		DmMobile mobile = null;
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			
			mobile = gson.fromJson(gson.toJson(obj), DmMobile.class);
			mobile.set_id(obj.get("_id"));
			
			mobiles.put(mobile.getKey(), mobile);
		}
		
        return mobiles;		
	}
	
	
	public static void main(String[] args) throws Exception {
		init_mongodb_pool();
		
		//System.out.println(gson.toJson(get_mongo_collection_dm_mobile().find(new BasicDBObject("Key", "1801623")).iterator().next()));
		//System.out.println(gson.toJson(get_mongo_collection_ds_mobile().find(new BasicDBObject("Cp", "13152656864")).iterator().next()));
		
		// 测试getDmMobileByKey
		/*System.out.println(gson.toJson(getDmMobileByKey("1625485")));
		System.out.println(gson.toJson(getDmMobileByKey("1300263")));
		System.out.println(gson.toJson(getDmMobileByKey(java.util.Arrays.asList("1625485", "1300263", "1801623"))));*/
	}
}
