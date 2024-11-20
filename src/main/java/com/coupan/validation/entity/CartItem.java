package com.coupan.validation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Long id;

    private int quantity;

    private double price;
    private double totalDiscount;


}
