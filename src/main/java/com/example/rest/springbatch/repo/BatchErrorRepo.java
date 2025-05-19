package com.example.rest.springbatch.repo;

import com.example.rest.springbatch.model.entity.BatchErrorTbl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchErrorRepo extends JpaRepository<BatchErrorTbl, Long> {
}
