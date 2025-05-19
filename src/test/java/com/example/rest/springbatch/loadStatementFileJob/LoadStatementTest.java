package com.example.rest.springbatch.loadStatementFileJob;

import com.example.rest.springbatch.util.service.BatchErrorLogService;
import lombok.Lombok;
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
public class LoadStatementTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("loadStatementFileJob")
    private Job loadStatementFileJob;

    @Autowired
    private BatchErrorLogService batchErrorLogService;

    /*@Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/loadStatementFileJob/create_test_case_data.sql"})
    public void testValidationErrorCases() throws Exception{
        log.info("----------------------testValidationErrorCases----------------------");
        //set test file and parameters
        String fileNameList = "STMT_FILE.ERROR_FIELD_VALIDATION.txt";
        String jobrefid = UUID.randomUUID().toString();
        String jobName = "loadStatementFileJob";

        TestPropertyValues.of("app.fileNameList=" + fileNameList).applyTo(env);
        TestPropertyValues.of("spring.batch.jobName=" + jobName).applyTo(env);

        //run batch job
        JobParameters jobParameters = new JobParametersBuilder().addString("fileNameList", fileNameList)
                .addString("jobrefid", jobrefid).toJobParameters();
        JobExecution jobExecution = jobLauncher.run(loadStatementFileJob, jobParameters);

        //check test results
        assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());

        Long jobId = jobExecution.getJobId();

        final int errorCountStatusInvalid = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "tbl_error",
                "job_id=" + jobId + " AND status='INVALID'");
        log.info("testValidationErrorCases#errorCountStatusInvalid:{}", errorCountStatusInvalid);
        assertEquals(5, errorCountStatusInvalid);

        log.info("----------------------testValidationErrorCases end----------------------");
    }*/

//    @Test
//    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
//            "classpath:sql/loadStatementFileJob/create_test_case_data.sql"
//    })
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/common/delete_common_tables.sql",
            "classpath:sql/common/01_create_tables.sql"})
    public void testJobSuccess() throws Exception{
        log.info("----------------------testJobSuccess----------------------");
        //set test file and parameters
        String fileNameList = "STMT_FILE.SUCCESS.txt";
        String jobrefid = UUID.randomUUID().toString();
        String jobName = "loadStatementFileJob";

        TestPropertyValues.of("app.fileNameList=" + fileNameList).applyTo(env);
        TestPropertyValues.of("spring.batch.jobName=" + jobName).applyTo(env);

        //run batch job
        JobParameters jobParameters = new JobParametersBuilder().addString("fileNameList", fileNameList)
                .addString("jobrefid", jobrefid).toJobParameters();
        JobExecution jobExecution = jobLauncher.run(loadStatementFileJob, jobParameters);
        
        //Force Flush the error queue to DB
        batchErrorLogService.forceConsume();

        //check test results
        assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());

        Long jobId = jobExecution.getJobId();

        final int batchErrorTable = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "batch_error_tbl",
                "job_id=" + jobId + " AND status='INVALID'");
        log.info("testJobSuccess#errorTable:{}", batchErrorTable);
        assertEquals(1, batchErrorTable);

        final int rowsInsertedinTblStatement = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "tbl_statement",
                "created_by='" + jobId + "'");
        log.info("testJobSuccess#tblStatement:{}", rowsInsertedinTblStatement);
        assertEquals(3, rowsInsertedinTblStatement);

        final int rowsStatusUpdatedinTblStatement = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "tbl_statement",
                "status='Generated'");
        log.info("testJobSuccess#tblStatement:{}", rowsStatusUpdatedinTblStatement);
        assertEquals(3, rowsStatusUpdatedinTblStatement);

        log.info("----------------------testJobSuccess end----------------------");
    }
}
