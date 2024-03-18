package com.ruc.jpa.service;

import com.ruc.jpa.entity.Product;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProductService {
    private AtomicInteger insertCount = new AtomicInteger(0);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(rollbackOn = {Exception.class})
    public int batchSave(List<Product> list) {
        int batchSize = 10000; // 设置组大小，分组插入
        boolean hasPersist = false;
        for (Product product : list) {
            entityManager.persist(product); // 减少一步查询操作
            int currCount = insertCount.incrementAndGet();
            hasPersist = true;

            if(currCount % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
                hasPersist = false;
            }
        }

        if(hasPersist) {
            entityManager.flush();
            entityManager.clear();
        }
        return 1;
    }

    /**
     * 生成 insert into product(item_id, title, pictUrl) values (?,?,?), (?,?,?), (?,?,?)...
     * 类似形式的 SQL，再设置参数
     */
    @Transactional(rollbackOn = {Exception.class})
    public void batchSaveWithSql(List<Product> list) {
        String sql = buildSQL(list);

        Query nativeQuery = entityManager.createNativeQuery(sql);

        // populate parameter
        String[] fields = getFields();
        int fieldLength = fields.length;
        // parameter index start from 1
        int position = 0;
        for (Product product : list) {
            nativeQuery.setParameter(position + 1, product.getItemId());
            nativeQuery.setParameter(position + 2, product.getTitle());
            nativeQuery.setParameter(position + 3, product.getPictUrl());
            nativeQuery.setParameter(position + 4, product.getCategory());
            nativeQuery.setParameter(position + 5, product.getBrandId());
            nativeQuery.setParameter(position + 6, product.getStore());
            position += fieldLength;
        }

        nativeQuery.executeUpdate(); // TODO uncomment
    }

    private String buildSQL(List<Product> list) {
        // insert into product(
        StringBuilder sqlBuilder = getInsertSQLPrefix();

        // concat column
        // 前提：需要在实体类中的每个数据库字段上都标注 @Column 注解
        // insert into product(item_id, title, pictUrl)
        String[] fields = getFields();
        for (String field : fields) {
            sqlBuilder.append(field).append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        // close column
        sqlBuilder.append(")").append(" values ");

        // concat placeholder
        String placeholder = generatePlaceholder(fields.length);

        for (int i = 0; i < list.size(); i++) {
            sqlBuilder.append(placeholder).append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);

        return sqlBuilder + ";";
    }

    private StringBuilder getInsertSQLPrefix() {
        String table = getTableName();
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table).append("(");

        return sb;
    }

    private String getTableName() {
        Class<? extends Product> prodClass = Product.class;
        Table tableAnnotation = prodClass.getAnnotation(Table.class);
        return tableAnnotation.name();
    }

    private String[] getFields() {
        List<String> fieldList = new ArrayList<>();
        Class<Product> productClass = Product.class;
        Field[] fields = productClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String name = Objects.nonNull(column.name()) ? column.name() : field.getName();
                fieldList.add(name);
            }
        }
        return fieldList.toArray(new String[0]);
    }

    private String generatePlaceholder(int columnLength) {
        StringBuilder phBuilder = new StringBuilder("(");
        // (?,
        for (int i = 0; i < columnLength; i++) {
            phBuilder.append("?,");
        }
        // (?,?,?,?,?)
        phBuilder.deleteCharAt(phBuilder.length() - 1).append(")");
        return phBuilder.toString();
    }
}
