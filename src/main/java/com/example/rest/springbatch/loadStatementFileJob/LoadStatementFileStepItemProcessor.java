package com.example.rest.springbatch.loadStatementFileJob;

import com.example.rest.springbatch.config.AppConstants;
import com.example.rest.springbatch.model.entity.TblStatementEntity;
import com.example.rest.springbatch.model.item.StatementFileInput;
import com.example.rest.springbatch.util.cache.DbCacheService;
import com.example.rest.springbatch.util.service.BatchErrorLogService;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.Set;

import static com.example.rest.springbatch.config.AppConstants.SERVICE_TYPE_ST;

@Slf4j
public class LoadStatementFileStepItemProcessor implements ItemProcessor<StatementFileInput, TblStatementEntity> {
    private JobExecution jobExecution;

    @Autowired
    private Validator validator;

    @Autowired
    private DbCacheService dbCacheService;

    @Autowired
    private BatchErrorLogService batchErrorLogService;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        jobExecution = stepExecution.getJobExecution();
        log.info("@Before Step: getJobExecution={}", jobExecution);
    }

    @Value("${batch.app.loadStatementFileJob.loadStatementFileStep.processor.record-length}")
    private int recordLength;

    @Override
    public TblStatementEntity process(StatementFileInput item) throws Exception{
        log.debug("inside loadStatementFileStepItemProcessor");
        if(!fieldValidation(item)){
            return null;
        }
        return transform(item);
    }

    /*private boolean fieldValidation(StatementFileInput item) {
        log.debug("### Inside loadStatementFileStepItemProcessor: fieldValidation");

        String fileLine = item.getFileLine();
        if (fileLine == null) {
            String errorMsg = "File line is null for StatementFileInput: " + item;
            log.error(errorMsg);
            batchErrorLogService.logError("loadStatementFileStep", errorMsg);
            return false; // validation failed
        }

        int lineLength = fileLine.length();
        if (lineLength != recordLength) {
            String errorMsg = String.format("Invalid line length [%d] for record: [%s], expected: [%d]",
                    lineLength, fileLine, recordLength);

            log.error(errorMsg);
            batchErrorLogService.logError("loadStatementFileStep", errorMsg);
            return false; // validation failed
        }
        return true; // validation passed
    }*/

    private boolean fieldValidation(StatementFileInput item) {
        log.debug("### Inside loadStatementFileStepItemProcessor: fieldValidation");

        int lineLength = item.getFileLine().length();
        if(lineLength != recordLength){
            batchErrorLogService.logError(item, AppConstants.BATCH_ERROR_TBL, AppConstants.STATUS_INVALID,
                    "Expected record size is:" + recordLength, jobExecution);
            return false; // validation failed
        }
        Set<ConstraintViolation<StatementFileInput>> constraintViolations = validator.validate(item);
        if(constraintViolations.size() > 0){
            batchErrorLogService.logError(item, AppConstants.TABLE_STATEMENT, constraintViolations, 
                    jobExecution);
            return false;
        }
        return true; // validation passed
    }


    private TblStatementEntity transform(StatementFileInput input){
        log.debug("### Inside loadStatementFileStepItemProcessor: transform");

        TblStatementEntity item = new TblStatementEntity();

        item.setServiceType(SERVICE_TYPE_ST);
        item.setYearMonth(dbCacheService.findBatchYearCurrentMonth());

        item.setAccountNo(input.getAccountNo());
        item.setProductCode(input.getProductCode());
        item.setStmtType(input.getStmtType());
        item.setStmtDate(input.getStmtDate());
        item.setStmtInd(input.getIndicator());
        
        item.setStatus("Generated");

        item.setCreatedBy(jobExecution.getId().toString());
        item.setCreatedTs(LocalDateTime.now());
        item.setUpdatedBy(jobExecution.getId().toString());
        item.setUpdatedTs(LocalDateTime.now());

        return item;
    }
}
