package com.commerce.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 150)
    private String categoryName;

    protected Product() {}

    public Product(String name, BigDecimal price, Integer stock, Long categoryId, String categoryName) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId()               { return id; }
    public String getName()           { return name; }
    public BigDecimal getPrice()      { return price; }
    public Integer getStock()         { return stock; }
    public Long getCategoryId()       { return categoryId; }
    public String getCategoryName()   { return categoryName; }

    public void setName(String name)                 { this.name = name; }
    public void setPrice(BigDecimal price)            { this.price = price; }
    public void setStock(Integer stock)               { this.stock = stock; }
    public void setCategoryId(Long categoryId)        { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName)  { this.categoryName = categoryName; }
}
