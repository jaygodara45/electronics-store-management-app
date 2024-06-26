package com.godara.crud_operations.services;

import com.godara.crud_operations.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product, Integer> {

}
