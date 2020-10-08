package com.mongodb.mongodbdemo.dao.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.mongodbdemo.dao.AbstractInventoryDao;
import com.mongodb.mongodbdemo.dao.InventoryDao;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InventoryDaoImpl extends AbstractInventoryDao  implements InventoryDao  {

    public static String INVENTORY_COLL = "product";

    MongoCollection<Document> inventoryCollection;

    @Autowired
    public InventoryDaoImpl(MongoClient mongoClient,
                            @Value("${spring.mongodb.database}") String db){
        super(mongoClient, db);
        inventoryCollection = mongoDatabase.getCollection(INVENTORY_COLL);
    }


    @Override
    public Long getProductCount() {
        return inventoryCollection.countDocuments();
    }

    @Override
    public List<Document> getProductByName(String name){
        Bson filter = Filters.eq("name", name);

        List<Document> documents = new ArrayList<>();

        inventoryCollection
                .find(filter)
                .iterator()
                .forEachRemaining(documents::add);

        return documents;
    }
}
