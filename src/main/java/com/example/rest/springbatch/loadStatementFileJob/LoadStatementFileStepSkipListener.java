package com.example.rest.springbatch.loadStatementFileJob;

import com.example.rest.springbatch.model.entity.TblStatementEntity;
import com.example.rest.springbatch.model.item.LineDetailBaseItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Slf4j
public class LoadStatementFileStepSkipListener implements SkipListener<LineDetailBaseItem, List<TblStatementEntity>> {

//    @Value("#{stepExecution.jobExecution}")
//    private JobExecution jobExecution;

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Skipped during read", t);
    }

    @Override
    public void onSkipInWrite(List<TblStatementEntity> item, Throwable t) {
        log.error("Skipped during write: {}", item, t);
    }

    @Override
    public void onSkipInProcess(LineDetailBaseItem item, Throwable t) {
        log.error("Skipped during process: {}", item, t);
//        log.debug("JobExecution ID: {}", jobExecution.getJobId());
    }
}
