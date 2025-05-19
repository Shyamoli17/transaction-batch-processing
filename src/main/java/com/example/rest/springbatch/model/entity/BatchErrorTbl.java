package com.example.rest.springbatch.model.entity;

import com.example.rest.springbatch.config.AppConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = AppConstants.BATCH_ERROR_TBL)
public class BatchErrorTbl implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batch_error_tbl_seq_pooled_generator")
    @GenericGenerator(
            name = "batch_error_tbl_seq_pooled_generator",
            type = SequenceStyleGenerator.class,
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "seq_batch_error_tbl"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled")
            }
    )
    private Long id;

    @Column(name = "job_id")
    private Long jobExecutionId;

    @Column(name = "job_name")
    private String jobName;
    
    @Column(name = "filename")
    private String filename;

    @Column(name = "row_no")
    private Long rowNo;

    @Column(name = "row_data")
    private String rowData;

    @Column(name = "target_table")
    private String targetTable;

    @Column(name = "status")
    private String status;
    
    @Column(name = "err_desc")
    private String errDesc;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

}
