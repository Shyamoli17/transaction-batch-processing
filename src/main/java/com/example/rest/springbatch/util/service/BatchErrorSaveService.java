package com.example.rest.springbatch.util.service;

import com.example.rest.springbatch.config.AppConstants;
import com.example.rest.springbatch.model.entity.BatchErrorTbl;
import com.example.rest.springbatch.model.item.LineDetailBaseItem;
import com.example.rest.springbatch.repo.BatchErrorRepo;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BatchErrorSaveService {

    @Autowired
    private BatchErrorRepo batchErrorRepo;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveErr(List<BatchErrorTbl> errorList){
        if(errorList != null && errorList.size() > 0){
            batchErrorRepo.saveAll(errorList);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveErr(BatchErrorTbl error){
        if(error != null){
            batchErrorRepo.save(error);
        }
    }
}
