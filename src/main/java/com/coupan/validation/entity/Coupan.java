package com.coupan.validation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_coupan")
public class Coupan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;

    @ElementCollection
    private List<Long> productIds;
    @ElementCollection
    private List<Long> buyProducts;
    @ElementCollection
    private List<Long> getProducts;

    private Double discountPercentage;
    private Double thresholdAmount;

    private Integer buyQunatity;
    private Integer getQuantity;
    private Integer repetitionLimit;

    private Boolean isActive;

    private LocalDate coupanExpiryDate;

    public boolean isExpired() {
        return coupanExpiryDate != null && coupanExpiryDate.isBefore(LocalDate.now());
    }
}
