package com.coupan.validation.dto;

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
