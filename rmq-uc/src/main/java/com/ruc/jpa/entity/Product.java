package com.ruc.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product implements BaseEntity {
    @Serial
    private static final long serialVersionUID = 5448225905383591579L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "item_id")
    private Integer itemId;
    @Column(name = "title")
    private String title;
    @Column(name = "pict_url")
    private String pictUrl;
    @Column(name = "category")
    private String category;
    @Column(name = "brand_id")
    private String brandId;
    // private String sellerId;
    @Column(name = "store")
    private Integer store; // 库存
}
