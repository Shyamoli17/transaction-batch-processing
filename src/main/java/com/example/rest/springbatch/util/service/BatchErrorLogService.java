package com.example.rest.springbatch.util.service;

import com.example.rest.springbatch.config.AppConstants;
import com.example.rest.springbatch.model.entity.BatchErrorTbl;
import com.example.rest.springbatch.model.item.LineDetailBaseItem;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
public class BatchErrorLogService {

    private final static ExecutorService executorService = Executors.newFixedThreadPool(1);

    private final BlockingQueue<BatchErrorTbl> logQueue;
    
    private long lastConsumeTime;

    @Value("${batch.app.batch-error-log.multi-thread-flag:true}")
    private boolean multiThreadFlag;

    @Value("${batch.app.batch-error-log.chunksize:1000}")
    private int CHUNK_SIZE;

    @Value("${batch.app.batch-error-log.latencytime:500}")
    private long LATENCY_TIME;

    @Autowired
    public BatchErrorLogService() {
        logQueue = new LinkedBlockingDeque<BatchErrorTbl>();
        lastConsumeTime = 0;
    }

    @Autowired
    private BatchErrorSaveService batchErrorSaveService;
    
    public void logError(LineDetailBaseItem item, String targetTable, String status, String err_desc,
                         JobExecution jobExecution){
        BatchErrorTbl error = new BatchErrorTbl();
        error.setJobExecutionId(jobExecution.getId());
        error.setJobName(jobExecution.getJobInstance().getJobName());
        error.setFilename(jobExecution.getJobParameters().getString("fileNameList"));
        error.setRowNo((long) item.getFileLineNumber());
        error.setRowData(item.getFileLine());
        error.setTargetTable(targetTable);
        error.setStatus(status);
        error.setErrDesc(err_desc);
        error.setCreatedTs(LocalDateTime.now());
        
        log.error("save error into DB: {} ", error);
        
        this.produce(error);
    }

    public void logError(FlatFileParseException ffpe, String targetTable, String status, String err_desc,
                         JobExecution jobExecution){
        BatchErrorTbl error = new BatchErrorTbl();
        error.setJobExecutionId(jobExecution.getId());
        error.setJobName(jobExecution.getJobInstance().getJobName());
        error.setFilename(jobExecution.getJobParameters().getString("fileNameList"));
        error.setRowNo((long) ffpe.getLineNumber());
        error.setRowData(ffpe.getInput());
        error.setTargetTable(targetTable);
        error.setStatus(status);
        error.setErrDesc(err_desc);
        error.setCreatedTs(LocalDateTime.now());

        log.error("save error into DB: {} ", error);

        this.produce(error);
    }

    public <T> void logError(LineDetailBaseItem item, String targetTable, Set<ConstraintViolation<T>> constraintVoilations,
                         JobExecution jobExecution){
        BatchErrorTbl error = new BatchErrorTbl();
        error.setJobExecutionId(jobExecution.getId());
        error.setJobName(jobExecution.getJobInstance().getJobName());
        error.setFilename(jobExecution.getJobParameters().getString("fileNameList"));
        error.setRowNo((long) item.getFileLineNumber());
        error.setRowData(item.getFileLine());
        log.error("### row_data: {} ", item.getFileLine());
        error.setTargetTable(targetTable);
        error.setStatus(AppConstants.STATUS_INVALID);
        String errMsgs = constraintVoilations.stream().map(a -> a.getMessage()).collect(Collectors.joining("; "));
        error.setErrDesc(errMsgs);
        error.setCreatedTs(LocalDateTime.now());

        log.error("save error into DB: {} ", error);

        this.produce(error);
    }
    
    private void produce(BatchErrorTbl error){
        logQueue.add(error);
        if(!multiThreadFlag){
            consume();
            return;
        }
        if(logQueue.size() >= CHUNK_SIZE && (System.currentTimeMillis() - lastConsumeTime) > LATENCY_TIME){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    consume();
                }
            });
        }
    }
    
    @Scheduled(fixedRateString = "${batch.app.batch-error-log.latencytime:500}")
    public void ScheduleConsume(){
        if(logQueue.size() > 0 && (System.currentTimeMillis() - lastConsumeTime) > LATENCY_TIME){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    consume();
                }
            });
        }
    }
    
    public void forceConsume(){
        if (logQueue.size() > 0){
            consume();
        }
    }

    private synchronized void consume(){
        if (logQueue.size() > 0){
            List<BatchErrorTbl> errorList = new ArrayList<>();
            log.info("Consuming errors from queue, size: {}", logQueue.size());
            logQueue.drainTo(errorList, CHUNK_SIZE);
            if (errorList != null && errorList.size() > 0){
                log.info("Saving {} errors to DB", errorList.size());
                batchErrorSaveService.saveErr(errorList);
                lastConsumeTime = System.currentTimeMillis();
            }
            if(logQueue.size() >= CHUNK_SIZE){
                consume();
            }
        }
    }
    
}
