package com.mongodb.mongodbdemo.controller;

import com.mongodb.mongodbdemo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    InventoryService inventoryService;

    @GetMapping("/product")
    public Long getProductCount(){
        return inventoryService.getProductCount();
    }
}
