package com.coupan.validation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private List<CartItem> cartItemList;

    private double totalPrice;
    private double totalDiscount;

    private double finalPrice;
}
