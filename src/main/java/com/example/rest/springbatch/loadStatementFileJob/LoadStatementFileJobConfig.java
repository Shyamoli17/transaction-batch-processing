package com.example.rest.springbatch.loadStatementFileJob;

import com.example.rest.springbatch.model.entity.TblStatementEntity;
import com.example.rest.springbatch.model.item.StatementFileInput;
import com.example.rest.springbatch.util.customize.CustomFlatFileItemReader;
import com.example.rest.springbatch.util.fixedlength.formatters.FixedLength;
import com.example.rest.springbatch.util.fixedlength.formatters.FixedLengthLineMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Slf4j
@Configuration
public class LoadStatementFileJobConfig {

    @Autowired
    @Qualifier("jobRepository")
    JobRepository jobRepository;

    @Autowired
    @Qualifier("transactionManager")
    JpaTransactionManager transactionManager;

    @Autowired
    @Qualifier("jobStepTaskExecuter")
    TaskExecutor jobStepTaskExecuter;

    @Autowired
    DataSource dataSource;

    @Bean
    Job loadStatementFileJob(JobRepository jobRepository,
                             @Qualifier("loadStatementFileStep") Step loadStatementFileStep) {
        return new JobBuilder("loadStatementFileJob", jobRepository)  // Renamed to "loadStatementFileJob"
                .start(loadStatementFileStep)
                .build();
    }

    @Value("${batch.app.loadStatementFileJob.loadStatementFileStep.chunksize}")
    private int chunkSize;

    @Value("${batch.app.loadStatementFileJob.loadStatementFileStep.reader.basepath}")
    private String readerBasePath;

    @Value("${batch.app.loadStatementFileJob.loadStatementFileStep.reader.header-prefix}")
    private String readerHeaderPrefix;

    @Value("${batch.app.loadStatementFileJob.loadStatementFileStep.reader.footer-prefix}")
    private String readerTrailerPrefix;

    @Value("${batch.app.loadStatementFileJob.loadStatementFileStep.writer.sql}")
    private String writerSql;

    @Bean
    @JobScope
    public Step loadStatementFileStep(
            @Qualifier("loadStatementFileStepItemReader") CustomFlatFileItemReader<StatementFileInput> loadStatementFileStepItemReader,
            @Qualifier("loadStatementFileStepItemProcessor") LoadStatementFileStepItemProcessor loadStatementFileStepItemProcessor,
            @Qualifier("loadStatementFileStepWriter") JdbcBatchItemWriter<TblStatementEntity> loadStatementFileStepWriter,
            @Qualifier("loadStatementFileStepSkipListener") LoadStatementFileStepSkipListener loadStatementFileStepSkipListener
    ) {
        return new StepBuilder("loadStatementFileStep", jobRepository)
                .<StatementFileInput, TblStatementEntity>chunk(chunkSize, transactionManager)
                .reader(loadStatementFileStepItemReader)
                .processor(loadStatementFileStepItemProcessor)
                .writer(loadStatementFileStepWriter)
                .faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .listener(loadStatementFileStepSkipListener)
//                .taskExecutor(jobStepTaskExecuter) // <-- Added
                .build();

    }

    @Bean
    @StepScope
    public CustomFlatFileItemReader<StatementFileInput> loadStatementFileStepItemReader(
            @Value("#{jobParameters[fileNameList]}") String fileNameList) {
        log.debug("inside loadStatementFileJobConfig - reader");

        if (fileNameList == null || fileNameList.trim().isEmpty()) {
            throw new IllegalArgumentException("Input file path must be provided as a job parameter 'fileNameList'");
        }
        String fullPath = "src/test/resources/infiles.loadStatementFileJob/" + fileNameList;
        File file = new File(fullPath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Input file does not exist: " + fullPath);
        }

        CustomFlatFileItemReader<StatementFileInput> itemReader = new CustomFlatFileItemReader<>();
        itemReader.setName("loadStatementFileStepItemReader");
        itemReader.setResource(new FileSystemResource(fullPath));
        itemReader.setEncoding(StandardCharsets.UTF_8.name());

        String[] headerAndFooterPrefix = {readerHeaderPrefix, readerTrailerPrefix};
        itemReader.setComments(headerAndFooterPrefix);

        itemReader.setSkippedLinesCallback(line -> log.warn("Skipped line: {}", line));

        // FIX: Properly initialize and set FixedLength
        FixedLength<StatementFileInput> fixedLength = new FixedLength<>();
        fixedLength.registerLineType(StatementFileInput.class);

        FixedLengthLineMapper<StatementFileInput> fixedLengthLineMapper = new FixedLengthLineMapper<>();
        fixedLengthLineMapper.setFixedlength(fixedLength); // <-- THIS LINE IS REQUIRED

        itemReader.setLineMapper(fixedLengthLineMapper);

        return itemReader;
    }

    @Bean
    @StepScope
    LoadStatementFileStepItemProcessor loadStatementFileStepItemProcessor() {
        return new LoadStatementFileStepItemProcessor();
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<TblStatementEntity> loadStatementFileStepWriter() {
        log.debug("Inside loadStatementFileStepWriter");
        JdbcBatchItemWriter<TblStatementEntity> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setDataSource(dataSource);
        jdbcBatchItemWriter.setSql(writerSql);
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        jdbcBatchItemWriter.afterPropertiesSet();

        return jdbcBatchItemWriter;
    }

    @Bean
    @StepScope
    LoadStatementFileStepSkipListener loadStatementFileStepSkipListener() {
        return new LoadStatementFileStepSkipListener();
    }

    @PostConstruct
    public void checkTableExists() {
        String tableName = "tbl_statement";
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(2)) {
                throw new IllegalStateException("Database connection is not valid!");
            }

            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, "transaction_processing", tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    System.out.println("Table " + tableName + " exists.");
                } else {
                    System.out.println("Table " + tableName + " does not exist.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check for table existence", e);
        }
    }
}
