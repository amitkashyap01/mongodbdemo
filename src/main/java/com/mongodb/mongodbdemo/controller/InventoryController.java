package com.mongodb.mongodbdemo.controller;

import com.mongodb.mongodbdemo.service.InventoryService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    InventoryService inventoryService;

    @GetMapping("/product")
    public Long getProductCount(){
        return inventoryService.getProductCount();
    }

    @GetMapping("/product/{productName}")
    public List<Document> getProductByName(@PathVariable String productName){
        return inventoryService.getProductByName(productName);
    }
}
