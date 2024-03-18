package com.ruc.jpa.repository;

import com.ruc.jpa.entity.Product;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends BaseRepository<Product, Integer> {
}
