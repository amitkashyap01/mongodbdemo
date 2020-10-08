package com.mongodb.mongodbdemo.dao;

import org.bson.Document;

import java.util.List;

public interface InventoryDao {
    Long getProductCount();
    List<Document> getProductByName(String name);
}
