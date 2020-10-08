package com.mongodb.mongodbdemo.service;

import org.bson.Document;

import java.util.List;

public interface InventoryService {
    Long getProductCount();
    List<Document> getProductByName(String name);
}
