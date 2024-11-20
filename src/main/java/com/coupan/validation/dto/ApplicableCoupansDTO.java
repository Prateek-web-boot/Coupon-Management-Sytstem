package com.coupan.validation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicableCoupansDTO {

    private long id;
    private String type;
    private Double discountOffered;
}
