package com.mongodb.mongodbdemo.service.impl;

import com.mongodb.mongodbdemo.dao.InventoryDao;
import com.mongodb.mongodbdemo.service.InventoryService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    InventoryDao inventoryDao;

    @Override
    public Long getProductCount() {
        return inventoryDao.getProductCount();
    }

    @Override
    public List<Document> getProductByName(String name){
        return inventoryDao.getProductByName(name);
    }
}
