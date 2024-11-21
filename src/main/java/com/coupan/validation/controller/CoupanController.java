package com.coupan.validation.controller;

import com.coupan.validation.dto.ApplicableCoupansDTO;
import com.coupan.validation.dto.Cart;
import com.coupan.validation.entity.Coupan;
import com.coupan.validation.service.CoupanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupans")
public class CoupanController {

    @Autowired
    private CoupanService coupanService;

    @GetMapping("/get")
    public ResponseEntity<List<Coupan>> getAllCoupans() {
        return new ResponseEntity<>(coupanService.getAllCoupans(), HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Coupan> getCoupanByID(@PathVariable("id") Long id) {
        Coupan coupan = coupanService.getCoupanById(id);
        return new ResponseEntity<>(coupan, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Coupan> addCoupan(@RequestBody Coupan coupan) {
        Coupan newcoupan = coupanService.createCoupan(coupan);
        return new ResponseEntity<>(newcoupan, HttpStatus.CREATED);
    }


    @PutMapping ("/update")
    public ResponseEntity<Coupan> updateCoupan(@RequestBody Coupan coupan, @PathVariable("id") Long id) {
        Coupan updatedcoupan = coupanService.updateCoupan(coupan, id);
        return new ResponseEntity<>(updatedcoupan, HttpStatus.CREATED);
    }

    @DeleteMapping ("/delete")
    public ResponseEntity<String> deleteCoupan(@PathVariable("id") Long id) {
        String msg = coupanService.deleteCoupan(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }


    @PostMapping("/applicable-coupan")
    public ResponseEntity<List<ApplicableCoupansDTO>> applicableCoupans(@RequestBody Cart cart) {
        List<ApplicableCoupansDTO> validCoupans = coupanService.coupanApplicable(cart);
        return new ResponseEntity<>(validCoupans, HttpStatus.OK);
    }

    @PostMapping("/apply-coupan/{id}")
    public ResponseEntity<Cart> applySelectedCoupan(@RequestBody Cart cart, @PathVariable("id") long id) {
        Cart cart1 = coupanService.applyCoupan(cart, id);
        return new ResponseEntity<>(cart1, HttpStatus.OK);
    }


}
