package com.ruc;

import com.ruc.jpa.entity.Product;
import com.ruc.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest
public class RMQDBTest {

    @Test
    public void insert_product() {
        List<Product> list = read("../dataset/tianchi_2014001_rec_tmall_product.txt");
        if (CollectionUtils.isEmpty(list)) return;
        List<Product> subProd = list.subList(170000, 1000000);

        // 多线程 + SQL 拼接插入 10000 * 10 条数据

        long start = System.currentTimeMillis();
        // Product save = productRepository.save(null);
        // productRepository.saveAll(subProd); // 27s for 10k data // 八百万+ 数据超过 30 分钟都没有插入完成
        // doInsert1(subProd); // 25s for 10k data // 53634ms for 20k
        // doInsert2(subProd); // 4s for 10k data 👍 // 6432ms for 20k // 100K 18895ms

        // doInsert2 插入 100w 数据 SQL 拼接插入出现 Java heap space OOM
        // 如何解决？

        // 多线程 + SQL 拼接
        doInsert3(subProd); // 83w // 384213 ms

        long end = System.currentTimeMillis();
        System.out.println("cost time: " + (end - start));
    }

    @Autowired
    private ProducerService producerService;
    // 使用 EntityManager#persist
    public void doInsert1(List<Product> list) {
        producerService.batchSave(list);
    }

    private void doInsert2(List<Product> list) {
        producerService.batchSaveWithSql(list);
    }

    // 线程池 + SQL 拼接
    private void doInsert3(List<Product> list) {
        int listSize = list.size();

        int groupSize = 100000; // 10w 一组
        // 首先拆分成 10w 一组，任务均分
        int groupCount = listSize / groupSize; // 总共能分成多少组？
        // 解决边界数据，最后一组的数据
        int totalSize = groupSize * groupCount;
        int left = totalSize < listSize ? listSize - totalSize : 0;
        if (left > 0) {
            groupCount += 1;
        }
        Integer[] eachTask = new Integer[groupCount];
        Arrays.fill(eachTask, groupSize);
        if (left > 0) {
            eachTask[eachTask.length - 1] += left; // 最后一组多处理一些数据
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int fromIndex = 0;
        for (Integer currTask : eachTask) {
            // 当前线程需要处理多少数据？
            int toIndex = fromIndex + currTask;
            int finalFromIndex = fromIndex;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                log.info("thread: {}, handling list from: {}, to: {}", Thread.currentThread().getName(), finalFromIndex, toIndex);
                List<Product> products = list.subList(finalFromIndex, toIndex);
                producerService.batchSaveWithSql(products);
            });
            futures.add(future);
            fromIndex = toIndex;
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("run allOf failed, error: {}", e.getMessage());
        }
    }

    private List<Product> read(String filepath) {
        List<Product> list = new ArrayList<>();
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
                String currLine = null;
                while (null != (currLine = br.readLine())) {
                    String[] split = currLine.split("\u0001");
                    if (split.length == 6) {
                        Integer itemId = Integer.valueOf(split[0]);
                        String title = split[1];
                        String pictUrl = split[2];
                        String category = split[3];
                        String brandId = split[4];
                        Integer store = (int) (Math.random() * 10) * (int) (Math.random() * 10);
                        // String sellerId = split[5];
                        Product prod = Product.builder()
                                .itemId(itemId)
                                .title(title)
                                .pictUrl(pictUrl)
                                .category(category)
                                .brandId(brandId)
                                .store(store)
                                .build();
                        list.add(prod);
                    }
                }
            }
        } catch (IOException e) {
            log.error("read file: {} error: {}", filepath, e.getMessage());
        }
        return list;
    }

    @Test
    public void handle_0x01() {
        // 解决字符串中的 <0x01>
        // txt 中显示 <0x01>
        // Java 字符串中显示 \u0001
        String str = "6963038\u0001夏款 洋装   孕妇装   彩条 雪纺孕妇裙   新款 圆领 短袖 孕妇装   孕妇连衣裙 \u0001http://img01.taobaocdn.com/bao/uploaded/i3/16405022221355071/T16hymXE0cXXXXXXXX_!!0-item_pic.jpg\u000116-2787\u0001b26844\u0001s78718";
        for (String s : str.split("\u0001")) {
            System.out.println(s);
        }
    }
}
