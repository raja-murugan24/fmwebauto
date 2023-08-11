package com.qa.base.utils;


import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * Encapsulates methods required to get data from MongoDB
 *
 * @author ainapu01
 */
public class MongoUtils {

	private MongoClient mongoClient = null;
	private MongoDatabase mongoDatabase = null;

	public static void main(String[] args) {
		MongoUtils mongoUtils = new MongoUtils("member");
		MongoCollection<Document>  collection = mongoUtils.getCollection("MemberHelpAndSupportTest");
		FindIterable<Document> iterable = collection.find(eq("TestMethod", "testPlanSupportLinks"));
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
		    System.out.println(cursor.next().get("TestMethod"));
		}
	}
	
	/**
	 * Constructor to setup connection to MongoDB database using environment vairables
	 * 
	 */
	public MongoUtils() {
		String mongoDB = System.getenv("MONGO_DB");
		setUp(mongoDB);
	}

	/**
	 * Constructor to setup connection to specific MongoDB database
	 * 
	 * @param mongoDB
	 */
	public MongoUtils(String mongoDB) {
		setUp(mongoDB);
	}

	/**
	 * Setup mongodb client and database using environment variables
	 * 
	 * @param mongoDB - mongo database name as String
	 */
	private void setUp(String mongoDB) {
		String connectionString = null;
		if (mongoClient == null && mongoDB != null
				&& !"".equals(mongoDB)) {
			String user = System.getenv("MONGO_USER");
			String password = System.getenv("MONGO_PASSWORD");
			String server = System.getenv("MONGO_SERVER");
			String port = System.getenv("MONGO_PORT");
			if (user != null && !user.isEmpty()) {
				connectionString = "mongodb://" + user + ":" + password + "@" + server + ":" + port + "/?readPreference=primary&ssl=false&authSource=" + mongoDB;
			} else {
				connectionString = "mongodb://" + server + ":" + port + "/?readPreference=primary&ssl=false&authSource=" + mongoDB;
			}
			mongoClient = MongoClients.create(connectionString);
			mongoDatabase = mongoClient.getDatabase(mongoDB);
		}
	}

	/**
	 * Get mongo collection from mongodb
	 * 
	 * @param collectionName
	 * @return MongoCollection
	 */
	public MongoCollection getCollection(String collectionName) {
		return mongoDatabase.getCollection(collectionName);
	}

	/**
	 * Close the database connection
	 *
	 */
	public void tearDown() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}


}

