package com.ruc.controller;

import com.ruc.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/prod")
@RestController
public class ProducerController {

    @Autowired
    private ProducerService producerService;

    @GetMapping("/send/tx/{txId}/{msg}")
    public void sendTransaction(@PathVariable String txId, @PathVariable String msg) {
        producerService.sendTransaction(txId, msg);
    }
}
