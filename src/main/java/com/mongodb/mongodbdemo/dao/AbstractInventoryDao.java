package com.mongodb.mongodbdemo.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Configuration;

@Configuration
public abstract class AbstractInventoryDao {

    protected MongoDatabase mongoDatabase;
    protected final String databaseName;
    protected MongoClient mongoClient;

    public AbstractInventoryDao(MongoClient mongoClient, String databaseName){
        this.databaseName = databaseName;
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoClient.getDatabase(this.databaseName);
    }
}
