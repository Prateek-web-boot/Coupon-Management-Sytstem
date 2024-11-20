package com.coupan.validation.repository;

import com.coupan.validation.entity.Coupan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoupanRepo extends JpaRepository<Coupan, Long> {

    public Coupan findByType(String type);

    public List<Coupan> findByIsActiveTrue();
}
