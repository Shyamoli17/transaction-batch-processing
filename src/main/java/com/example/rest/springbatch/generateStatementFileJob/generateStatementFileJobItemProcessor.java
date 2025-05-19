
package com.example.rest.springbatch.generateStatementFileJob;

import com.example.rest.springbatch.model.entity.TblStatementEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ItemProcessor for the generateStatementFileJob.
 * Sets status, updatedBy, and updatedTs fields before writing.
 * Note: For multi-threaded steps, storing JobExecution as a field is not thread-safe.
 */
@Slf4j
public class generateStatementFileJobItemProcessor implements ItemProcessor<TblStatementEntity, TblStatementEntity> {

    private JobExecution jobExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        jobExecution = stepExecution.getJobExecution();
        log.info("@Before Step: getJobExecution={}", jobExecution);
    }

    @Override
    public TblStatementEntity process(TblStatementEntity item) {
        item.setStatus("Exported");
        item.setUpdatedBy(jobExecution.getId().toString());
        item.setUpdatedTs(LocalDateTime.now());
        log.debug("Processed statement ID {}: status set to Exported, updatedBy={}, updatedTs={}",
                item.getId(), item.getUpdatedBy(), item.getUpdatedTs());
        return item;
    }
}
