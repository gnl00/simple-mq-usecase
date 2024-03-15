package com.ruc.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Integer id;
    private Integer itemId;
    private String title;
    private String pictUrl;
    private String category;
    private String brandId;
    private String sellerId;
}
