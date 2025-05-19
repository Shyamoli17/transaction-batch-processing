package com.example.rest.springbatch.computeStatementFeeJob;

import com.example.rest.springbatch.util.service.BatchErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class computeStatementFeeJobTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("computeStatementFeeJob")
    private Job computeStatementFeeJob;

    @Autowired
    private BatchErrorLogService batchErrorLogService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/common/01_create_tables.sql",
            "classpath:sql/common/delete_common_tables.sql",
            "classpath:sql/computeStatementFeeJob/create_test_case_data.sql"})
    public void testJobSuccess() throws Exception{
        log.info("----------------------computeStatementFeeJob start----------------------");
        
        //set test file and parameters
        String jobrefid = UUID.randomUUID().toString();
        String jobName = "computeStatementFeeJob";

        TestPropertyValues.of("spring.batch.jobName=" + jobName).applyTo(env);

        //run batch job
        JobParameters jobParameters = new JobParametersBuilder().addString("jobrefid", jobrefid).toJobParameters();
        JobExecution jobExecution = jobLauncher.run(computeStatementFeeJob, jobParameters);
        
        //Force Flush the error queue to DB
        batchErrorLogService.forceConsume();

        //check test results
        assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());

        Long jobId = jobExecution.getJobId();

        final int batchErrorTable = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "batch_error_tbl",
                "job_id=" + jobId + " AND status='INVALID'");
        log.info("testJobSuccess#errorTable:{}", batchErrorTable);
        assertEquals(0, batchErrorTable);

        final int rowsUpdatedinTblStatement = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "tbl_statement",
                "fee_amt= '40.0'");
        log.info("testJobSuccess#tblStatement:{}", rowsUpdatedinTblStatement);
        assertEquals(1, rowsUpdatedinTblStatement);

        final int rowsStatusUpdatedinTblStatement = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "tbl_statement",
                "status= 'Computed'");
        log.info("testJobSuccess#tblStatement:{}", rowsStatusUpdatedinTblStatement);
        assertEquals(4, rowsStatusUpdatedinTblStatement);

        log.info("----------------------computeStatementFeeJob end----------------------");
    }
}
