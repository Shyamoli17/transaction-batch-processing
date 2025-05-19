package com.example.rest.springbatch.generateStatementFileJob;

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
public class generateStatementFileJobTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("generateStatementFileJob")
    private Job generateStatementFileJob;

    @Autowired
    private BatchErrorLogService batchErrorLogService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/common/01_create_tables.sql",
            "classpath:sql/common/delete_common_tables.sql",
            "classpath:sql/generateStatementFileJob/create_test_case_data.sql"})
    public void testJobSuccess() throws Exception{
        log.info("----------------------generateStatementFileJob start----------------------");
        
        //set test file and parameters
        String jobrefid = UUID.randomUUID().toString();
        String jobName = "generateStatementFileJob";
        String fileNameList = "OUTPUT_Statement_file_20250424.txt";
        String bizDate = "20250424";

        TestPropertyValues.of("spring.batch.jobName=" + jobName).applyTo(env);

        //run batch job
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobrefid", jobrefid)
                .addString("fileNameList", fileNameList)
                .addString("bizDate", bizDate)
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(generateStatementFileJob, jobParameters);
        
        //Force Flush the error queue to DB
        batchErrorLogService.forceConsume();

        //check test results
        assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());

        Long jobId = jobExecution.getJobId();

        final int batchErrorTable = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "batch_error_tbl",
                "job_id=" + jobId + " AND status='INVALID'");
        log.info("testJobSuccess#errorTable:{}", batchErrorTable);
        assertEquals(0, batchErrorTable);

        final int rowsStatusUpdatedinTblStatement = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "tbl_statement",
                "updated_by='" + jobId + "' AND status= 'Exported'");
        log.info("testJobSuccess#tblStatement:{}", rowsStatusUpdatedinTblStatement);
        assertEquals(4, rowsStatusUpdatedinTblStatement);

        log.info("----------------------generateStatementFileJob end----------------------");
    }
}
