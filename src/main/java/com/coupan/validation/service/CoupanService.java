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

    public List<ApplicableCoupansDTO> coupanApplicable(Cart cart){
        List<Coupan> activeCoupans = coupanRepo.findByIsActiveTrue();

        return activeCoupans.stream()
                        .filter(coupan -> !coupan.isExpired())
                                .map(coupan -> {
                                    double discount = calcDiscount(coupan, cart);
                                    if(discount > 0) {
                                        ApplicableCoupansDTO response = new ApplicableCoupansDTO();
                                        response.setId(coupan.getId());
                                        response.setType(coupan.getType());
                                        response.setDiscountOffered(discount);
                                        return response;
                                    } else {
                                        return null;
                                    }

    }). collect(Collectors.toList());
    }

    public double calcDiscount(Coupan coupan, Cart cart) {
        String type = coupan.getType();

        if(type.equalsIgnoreCase("cart-wise")) {
            return calcCartWiseDiscount(coupan, cart);
        }

        else if(type.equalsIgnoreCase("product-wise")) {
            return calcProductWiseDiscount(coupan, cart);
        }

        else if(type.equalsIgnoreCase("BxGy")) {
            return calcBxGyDiscount(coupan, cart);
        }
        return 0;
    }


    public double calcCartWiseDiscount(Coupan coupan, Cart cart) {
        double totalCartValue =  cart.getCartItemList().stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();

        if(totalCartValue > coupan.getThresholdAmount()) {
            return totalCartValue * (coupan.getDiscountPercentage()/100);
        }
        return 0;
    }


    public double calcProductWiseDiscount(Coupan coupan, Cart cart) {

        boolean checkApplicable = coupan.getProductIds()
                .stream()
                .anyMatch(productId  -> cart.getCartItemList()
                        .stream()
                        .anyMatch(cartItem -> cartItem.getId().equals(productId)));

        double discount = 0.0;

        if(checkApplicable) {
            for(CartItem items: cart.getCartItemList()) {
                if(coupan.getProductIds().contains(items.getId())) {
                    double productValue = items.getQuantity() * items.getPrice() ;
                    discount += productValue * (coupan.getDiscountPercentage() /100);
                }
            }
        }
        return discount;
    }


    public double calcBxGyDiscount(Coupan coupan, Cart cart) {
        int productCountFromBuyList = 0;

        // calclulating total num of products biught from BuyList
        for (CartItem item : cart.getCartItemList()) {
            if (coupan.getBuyProducts().contains(item.getId())) {
                productCountFromBuyList += item.getQuantity();
            }
        }

        int coupanApplicableTimes = 0;

        if (coupan.getBuyQunatity() > 0) {
            coupanApplicableTimes = Math.min(productCountFromBuyList / coupan.getBuyQunatity(), coupan.getRepetitionLimit());
        } else {
            System.err.println("Buy quantity cannot be zero.");
        }
        int freeItemsQuantity = coupanApplicableTimes * coupan.getGetQuantity();

        // calculating discount value based on items in GetList
        double discount = 0;

        for(CartItem item : cart.getCartItemList()) {
            if(coupan.getGetProducts().contains(item.getId()) && freeItemsQuantity > 0) {

                int itemsFreeCount = Math.min(freeItemsQuantity, item.getQuantity());
                discount += itemsFreeCount * item.getPrice();
                freeItemsQuantity -= itemsFreeCount;
            }
        }
        return discount;
    }


// coupan applying to the cart Items

    public Cart applyCoupan(Cart cart, long id) {

        Optional<Coupan> couponDetails = coupanRepo.findById(id);
        Cart updatedCart = new Cart();

        double discount = 0;

        if(couponDetails.isPresent()) {
            Coupan coupon = couponDetails.get();
            List<CartItem> cartItems = cart.getCartItemList();
            List<CartItem> updatedCartItemList = new ArrayList<>();

            Long productIdForProductWiseDiscount = 0L;

            if(coupon.getType().equals("cart-wise")) {
                discount = calcCartWiseDiscount(coupon, cart);
            }

            if(coupon.getType().equals("product-wise")) {
                discount = calcProductWiseDiscount(coupon, cart);
                for(CartItem uniqueItem: cartItems) {
                    if(coupon.getProductIds().contains(uniqueItem.getId())) {
                        productIdForProductWiseDiscount = uniqueItem.getId();
                    }
                }
            }

            int freeItemsQuantity=0;

            if(coupon.getType().equalsIgnoreCase("BxGy")) {
                discount = calcBxGyDiscount(coupon,cart);
                freeItemsQuantity = calculateFreeItemsQuantity(coupon, cart);
            }

            for(CartItem item: cartItems) {
                CartItem updatedCartItem = new CartItem();

                    updatedCartItem.setId(item.getId());
                    updatedCartItem.setPrice(item.getPrice());
                    updatedCartItem.setQuantity(item.getQuantity());

                    if(coupon.getType().equalsIgnoreCase("BxGy") &&
                            coupon.getGetProducts().contains(item.getId()) &&
                            freeItemsQuantity > 0) {

                        int itemsFreeCount = Math.min(freeItemsQuantity, item.getQuantity());
                        double itemDiscount = itemsFreeCount * item.getPrice();
                        updatedCartItem.setTotalDiscount(itemDiscount);
                    }

                    if(coupon.getType().equalsIgnoreCase("cart-wise") ||
                            item.getId().equals(productIdForProductWiseDiscount)) {

                        updatedCartItem.setTotalDiscount(item.getPrice() * item.getQuantity() * (coupon.getDiscountPercentage() /100));
                    }
                    updatedCartItemList.add(updatedCartItem);
            }

            double totalCartValue =  cart.getCartItemList().stream()
                    .mapToDouble(item -> item.getQuantity() * item.getPrice())
                    .sum();

            updatedCart.setCartItemList(updatedCartItemList);
            updatedCart.setTotalDiscount(discount);
            updatedCart.setTotalPrice(totalCartValue);
            updatedCart.setFinalPrice(totalCartValue - discount);
        }
        return updatedCart;
    }


    private int calculateFreeItemsQuantity(Coupan coupan, Cart cart) {
        int productCountFromBuyList = 0;

        for (CartItem item : cart.getCartItemList()) {
            if (coupan.getBuyProducts().contains(item.getId())) {
                productCountFromBuyList += item.getQuantity();
            }
        }

        if (coupan.getBuyQunatity() > 0) {
            int coupanApplicableTimes = Math.min(productCountFromBuyList / coupan.getBuyQunatity(), coupan.getRepetitionLimit());
            return coupanApplicableTimes * coupan.getGetQuantity();
        } else {
            System.err.println("Buy quantity cannot be zero.");
            return 0;
        }
    }

}
