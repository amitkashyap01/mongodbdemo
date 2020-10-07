package com.mongodb.mongodbdemo.dao.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.mongodbdemo.dao.InventoryDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InventoryDaoImpl implements InventoryDao {

    public static String INVENTORY_COLL = "product";

    MongoDatabase db;

    MongoCollection inventoryCollection;

    @Autowired
    public InventoryDaoImpl(MongoClient mongoClient){
        db = mongoClient.getDatabase("inventory");
        inventoryCollection = db.getCollection(INVENTORY_COLL);
    }


    @Override
    public Long getProductCount() {
        return inventoryCollection.countDocuments();
    }
}
