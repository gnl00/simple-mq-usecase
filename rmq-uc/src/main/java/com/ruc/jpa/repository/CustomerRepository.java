package com.ruc.jpa.repository;

import com.ruc.jpa.entity.Customer;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long>{
}
