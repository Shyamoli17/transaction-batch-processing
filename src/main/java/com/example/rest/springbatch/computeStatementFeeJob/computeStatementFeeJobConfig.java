
package com.example.rest.springbatch.computeStatementFeeJob;

import com.example.rest.springbatch.model.entity.TblStatementEntity;
import com.example.rest.springbatch.util.cache.DbCacheService;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class computeStatementFeeJobConfig {

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

    @Value("${batch.app.computeStatementFeeJob.computeStatementFeeJobStep.reader.chunksize}")
    private int chunkSize;

    @Value("${batch.app.computeStatementFeeJob.computeStatementFeeJobStep.writer.sql}")
    private String writerSql;

    @Bean
    public Job computeStatementFeeJob(
            @Qualifier("computeStatementFeeJobStep") Step computeStatementFeeJobStep) {
        return new JobBuilder("computeStatementFeeJob", jobRepository)
                .start(computeStatementFeeJobStep)
                .build();
    }

    @Bean
    @JobScope
    public Step computeStatementFeeJobStep(
            @Qualifier("computeStatementFeeJobItemReader") JpaPagingItemReader<TblStatementEntity> computeStatementFeeJobItemReader,
            @Qualifier("computeStatementFeeItemProcessor") ItemProcessor<TblStatementEntity, TblStatementEntity> computeStatementFeeItemProcessor,
            @Qualifier("computeStatementFeeJobItemWriter") JdbcBatchItemWriter<TblStatementEntity> computeStatementFeeJobItemWriter
    ) {
        return new StepBuilder("computeStatementFeeJobStep", jobRepository)
                .<TblStatementEntity, TblStatementEntity>chunk(chunkSize, jpaTransactionManager)
                .reader(computeStatementFeeJobItemReader)
                .processor(computeStatementFeeItemProcessor)
                .writer(computeStatementFeeJobItemWriter)
                .build();
    }

    @Bean(name = "computeStatementFeeJobItemReader")
    @JobScope
    public JpaPagingItemReader<TblStatementEntity> computeStatementFeeJobItemReader(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<TblStatementEntity>()
                .name("computeStatementFeeJobItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT t FROM TblStatementEntity t")
                .pageSize(chunkSize)
                .build();
    }

    @Bean(name = "computeStatementFeeItemProcessor")
    @JobScope
    public computeStatementFeeJobItemProcessor computeStatementFeeJobItemProcessor() {
        return new computeStatementFeeJobItemProcessor();
    }

    @Bean(name = "computeStatementFeeJobItemWriter")
    public JdbcBatchItemWriter<TblStatementEntity> computeStatementFeeJobItemWriter() {
        JdbcBatchItemWriter<TblStatementEntity> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql(writerSql);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }
}
