package com.ruc;

import com.ruc.jpa.entity.Product;
import com.ruc.jpa.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
// @SpringBootTest
public class RMQUCTest {
    @Autowired
    private ProductRepository productRepository;
    @Test
    public void test() {
        List<Product> products = productRepository.findAll();
        System.out.println(products);
    }

    @Test
    public void insert_product() {
        read("../dataset/tianchi_2014001_rec_tmall_product.txt");
        // Product save = productRepository.save(null);
    }

    private void read(String filepath) {
        try (
                RandomAccessFile raf = new RandomAccessFile(filepath, "r");
                FileReader fileReader = new FileReader(filepath);
                BufferedReader br = new BufferedReader(fileReader);
        ) {
            long length = raf.length();
            log.info("file length: {}, integer max: {}", length, Integer.MAX_VALUE);
            if (length > Integer.MAX_VALUE) {
                log.info("file too large, use split method...");
            } else {
//                int count = 200;
//                int len = 0;
//                while ((len = br.read()) != -1 && count > 0) {
//                    byte[] buffer = new byte[(int)len];
//                    System.out.println(new String(buffer));
//                    count--;
//                }
//                 byte[] buffer = new byte[(int)length];
                String currLine = br.readLine();
                String[] split = currLine.split(" \0x01 ");
                for (String s : split) {
                    System.out.println(s);
                }
            }
        } catch (IOException e) {
            log.error("read file: {} error: {}", filepath, e.getMessage());
        }
    }

    @Test
    public void handle_0x01() {
        // TODO 解决字符串中的 <0x01>
    }
}
