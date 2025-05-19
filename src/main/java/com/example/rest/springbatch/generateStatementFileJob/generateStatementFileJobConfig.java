
package com.example.rest.springbatch.generateStatementFileJob;

import com.example.rest.springbatch.computeStatementFeeJob.computeStatementFeeJobItemProcessor;
import com.example.rest.springbatch.config.AppConstants;
import com.example.rest.springbatch.model.entity.TblStatementEntity;
import com.example.rest.springbatch.util.cache.DbCacheService;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Configuration
public class generateStatementFileJobConfig {

    @Autowired
    @Qualifier("jobRepository")
    private JobRepository jobRepository;

    @Autowired
    @Qualifier("transactionManager")
    private JpaTransactionManager jpaTransactionManager;

    @Autowired
    private DbCacheService dbCacheService;

    @Autowired
    private DataSource dataSource;

    @Value("${batch.app.generateStatementFileJob.chunkSize:100}")
    private int chunkSize;

    @Value("${batch.app.generateStatementFileJob.outputFile.path:output/statements.txt}")
    private String outputFile;

    @Value("${batch.app.generateStatementFileJob.reader.sql}")
    private String readersql;

//    private static final String[] fileColumns = new String[]{"serviceType", "yearMonth", "accountNo", "productCode", "stmtInd", "stmtType", "stmtDate", "feeAmt"};
    
    @Bean
    public Job generateStatementFileJob(
            @Qualifier("generateStatementFileStep") Step generateStatementFileStep
    ) {
        return new JobBuilder("generateStatementFileJob", jobRepository)
                .start(generateStatementFileStep)
                .build();
    }

    @Bean
    @JobScope
    public Step generateStatementFileStep(
            @Qualifier("generateStatementFileJobItemReader") JpaPagingItemReader<TblStatementEntity> reader,
            @Qualifier("generateStatementFileJobItemProcessor") generateStatementFileJobItemProcessor generateStatementFileJobItemProcessor,
            @Qualifier("compositeItemWriter") ItemWriter<TblStatementEntity> compositeItemWriter
    ) {
        return new StepBuilder("generateStatementFileStep", jobRepository)
                .<TblStatementEntity, TblStatementEntity>chunk(chunkSize, jpaTransactionManager)
                .reader(reader)
                .processor(generateStatementFileJobItemProcessor)
                .writer(compositeItemWriter)
                .build();
    }

    @Bean(name = "generateStatementFileJobItemReader")
    @JobScope
    public JpaPagingItemReader<TblStatementEntity> generateStatementFileJobItemReader(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<TblStatementEntity>()
                .name("generateStatementFileJobItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(readersql)
                .pageSize(chunkSize)
                .build();
    }

    @Bean(name = "generateStatementFileJobItemProcessor")
    @JobScope
    public generateStatementFileJobItemProcessor generateStatementFileJobItemProcessor() {
        return new generateStatementFileJobItemProcessor();
    }

    @Bean(name = "fileItemWriter")
    @StepScope
    public FlatFileItemWriter<TblStatementEntity> fileItemWriter(
            @Value("${batch.app.generateStatementFileJob.outputFile.path}") String outputDir,
            @Value("#{jobParameters['fileNameList']}") String fileNameList
    ) {
        // Ensure outputDir ends with a separator
        if (!outputDir.endsWith("/") && !outputDir.endsWith("\\")) {
            outputDir = outputDir + File.separator;
        }
        String fullPath = outputDir + fileNameList;

        // Ensure parent directory exists
        File file = new File(fullPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        BeanWrapperFieldExtractor<TblStatementEntity> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
                "serviceType", "yearMonth", "accountNo", "productCode", "stmtInd", "stmtType", "stmtDate", "feeAmt"
        });

        DelimitedLineAggregator<TblStatementEntity> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter("|");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<TblStatementEntity>()
                .name("fileItemWriter")
                .resource(new FileSystemResource(fullPath))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write(
                        AppConstants.HEADER_TYPE_H
                                + AppConstants.DELIMETER_PIPE
                                + AppConstants.FILE_TYPE
                                + AppConstants.DELIMETER_PIPE
                                + LocalDate.now()
                ))
                .footerCallback(writer -> {
                    StepExecution stepExecution = StepSynchronizationManager.getContext() != null
                            ? StepSynchronizationManager.getContext().getStepExecution()
                            : null;
                    long count = stepExecution != null ? stepExecution.getWriteCount() : 0L;
                    String formattedCount = String.format("%04d", count);
                    writer.write(AppConstants.TRAILER_TYPE_T
                            + AppConstants.DELIMETER_PIPE
                            + formattedCount);
                })
                .build();
    }
    

    @Bean(name = "dbItemWriter")
    @JobScope
    public JpaItemWriter<TblStatementEntity> dbItemWriter(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        JpaItemWriter<TblStatementEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean(name = "compositeItemWriter")
    @StepScope
    public CompositeItemWriter<TblStatementEntity> compositeItemWriter(
            @Qualifier("fileItemWriter") FlatFileItemWriter<TblStatementEntity> fileItemWriter,
            @Qualifier("dbItemWriter") JpaItemWriter<TblStatementEntity> dbItemWriter
    ) {
        CompositeItemWriter<TblStatementEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(fileItemWriter, dbItemWriter));
        return compositeItemWriter;
    }
}
