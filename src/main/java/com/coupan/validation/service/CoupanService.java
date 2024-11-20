package com.coupan.validation.service;

import com.coupan.validation.dto.ApplicableCoupansDTO;
import com.coupan.validation.entity.Cart;
import com.coupan.validation.entity.CartItem;
import com.coupan.validation.entity.Coupan;
import com.coupan.validation.repository.CoupanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CoupanService {

    @Autowired
    private CoupanRepo coupanRepo;

    public List<Coupan> getAllCoupans() {
        List<Coupan> coupanList =  coupanRepo.findAll();
        return coupanList;
    }

    public Coupan getCoupanById(Long id) {
        Optional<Coupan> coupan =  coupanRepo.findById(id);

        if(coupan.isPresent()) {
            return coupan.get();
        } else {
            throw new RuntimeException("No coupan found with ID : " + id);
        }
    }

    public Coupan createCoupan(Coupan coupan) {
        Coupan newCoupan = coupanRepo.save(coupan);
        return newCoupan;
    }

    public Coupan updateCoupan(Coupan coupan, Long id) {

        return coupanRepo.findById(id).map(existingCoupan -> {
            existingCoupan.setType(coupan.getType());
            existingCoupan.setCoupanExpiryDate(coupan.getCoupanExpiryDate());
            existingCoupan.setDescription(coupan.getDescription());
            existingCoupan.setBuyQunatity(coupan.getBuyQunatity());
            existingCoupan.setGetQuantity(coupan.getGetQuantity());
            existingCoupan.setDiscountPercentage(coupan.getDiscountPercentage());
            existingCoupan.setRepetitionLimit(coupan.getRepetitionLimit());
            existingCoupan.setRepetitionLimit(coupan.getRepetitionLimit());
            existingCoupan.setIsActive(coupan.getIsActive());
            existingCoupan.setProductIds(coupan.getProductIds());

            return coupanRepo.save(existingCoupan);

        }).orElseThrow(() -> new RuntimeException("Coupan not FOUND !!"));
    }

    public String deleteCoupan(Long id) {
        Optional<Coupan> deletecoupan =  coupanRepo.findById(id);

        if(deletecoupan.isPresent()) {
            coupanRepo.deleteById(id);
            return "Coupan Deleted Successfully !";
        } else {
            throw new RuntimeException("No coupan found with ID : " + id);
        }
    }

    public List<ApplicableCoupansDTO> coupanApplicable(Cart cart) {
        List<Coupan> activeCoupans = coupanRepo.findByIsActiveTrue();

        return activeCoupans.stream()
                .filter(coupan -> !coupan.isExpired())
                .map(coupan -> {
                    double discount = calcDiscount(coupan, cart);
                    return discount > 0 ? createApplicableCoupansDTO(coupan, discount) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ApplicableCoupansDTO createApplicableCoupansDTO(Coupan coupan, double discount) {
        ApplicableCoupansDTO response = new ApplicableCoupansDTO();
        response.setId(coupan.getId());
        response.setType(coupan.getType());
        response.setDiscountOffered(discount);
        return response;
    }

    public double calcDiscount(Coupan coupan, Cart cart) {
        switch (coupan.getType().toLowerCase()) {
            case "cart-wise":
                return calcCartWiseDiscount(coupan, cart);
            case "product-wise":
                return calcProductWiseDiscount(coupan, cart);
            case "bxgy":
                return calcBxGyDiscount(coupan, cart);
            default:
                return 0;
        }
    }

    public double calcCartWiseDiscount(Coupan coupan, Cart cart) {
        double totalCartValue = cart.getCartItemList().stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();

        return totalCartValue > coupan.getThresholdAmount() ? totalCartValue * (coupan.getDiscountPercentage() / 100) : 0;
    }

    public double calcProductWiseDiscount(Coupan coupan, Cart cart) {
        return cart.getCartItemList().stream()
                .filter(item -> coupan.getProductIds().contains(item.getId()))
                .mapToDouble(item -> item.getQuantity() * item.getPrice() * (coupan.getDiscountPercentage() / 100))
                .sum();
    }

    public double calcBxGyDiscount(Coupan coupan, Cart cart) {
        int productCountFromBuyList = cart.getCartItemList().stream()
                .filter(item -> coupan.getBuyProducts().contains(item.getId()))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (coupan.getBuyQunatity() <= 0) {
            throw new IllegalArgumentException("Buy quantity cannot be zero.");
        }

        int coupanApplicableTimes = Math.min(productCountFromBuyList / coupan.getBuyQunatity(), coupan.getRepetitionLimit());
        int freeItemsQuantity = coupanApplicableTimes * coupan.getGetQuantity();

        double discount = 0;
        for (CartItem item : cart.getCartItemList()) {
            if (coupan.getGetProducts().contains(item.getId()) && freeItemsQuantity > 0) {
                int itemsFreeCount = Math.min(freeItemsQuantity, item.getQuantity());
                discount += itemsFreeCount * item.getPrice();
                freeItemsQuantity -= itemsFreeCount;
            }
        }
        return discount;
    }

    public Cart applyCoupan(Cart cart, long id) {
        Optional<Coupan> couponDetails = coupanRepo.findById(id);
        if (!couponDetails.isPresent()) {
            return cart;
        }

        Coupan coupon = couponDetails.get();
        double discount = calcDiscount(coupon, cart);
        List<CartItem> updatedCartItemList = cart.getCartItemList().stream()
                .map(item -> updateCartItem(item, coupon, discount))
                .collect(Collectors.toList());

        double totalCartValue = cart.getCartItemList().stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();

        Cart updatedCart = new Cart();
        updatedCart.setCartItemList(updatedCartItemList);
        updatedCart.setTotalDiscount(discount);
        updatedCart.setTotalPrice(totalCartValue);
        updatedCart.setFinalPrice(totalCartValue - discount);

        return updatedCart;
    }

    private CartItem updateCartItem(CartItem item, Coupan coupon, double discount) {
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(item.getId());
        updatedCartItem.setPrice(item.getPrice());
        updatedCartItem.setQuantity(item.getQuantity());

        if (coupon.getType().equalsIgnoreCase("BxGy") && coupon.getGetProducts().contains(item.getId())) {
            int freeItemsQuantity = calculateFreeItemsQuantity(coupon, item);
            int itemsFreeCount = Math.min(freeItemsQuantity, item.getQuantity());
            updatedCartItem.setTotalDiscount(itemsFreeCount * item.getPrice());
        } else if (coupon.getType().equalsIgnoreCase("cart-wise") || coupon.getProductIds().contains(item.getId())) {
            updatedCartItem.setTotalDiscount(item.getPrice() * item.getQuantity() * (coupon.getDiscountPercentage() / 100));
        }

        return updatedCartItem;
    }

    private int calculateFreeItemsQuantity(Coupan coupan, CartItem item) {
        int productCountFromBuyList = item.getQuantity();
        int coupanApplicableTimes = Math.min(productCountFromBuyList / coupan.getBuyQunatity(), coupan.getRepetitionLimit());
        return coupanApplicableTimes * coupan.getGetQuantity();
    }

}
