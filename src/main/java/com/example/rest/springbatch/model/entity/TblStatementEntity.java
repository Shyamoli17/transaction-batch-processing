package com.example.rest.springbatch.model.entity;

import com.example.rest.springbatch.model.item.LineDetailBaseItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Entity
@Table(name = "tbl_statement")
@AllArgsConstructor
@NoArgsConstructor
public class TblStatementEntity extends LineDetailBaseItem {
    private static final long serialVersionId = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbl_statement_seq_pooled_generator")
    @GenericGenerator(
            name = "tbl_statement_seq_pooled_generator",
            type = SequenceStyleGenerator.class,
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "seq_tbl_statement_id"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled")
            }
    )
    private Long id;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "year_month")
    private String yearMonth;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "stmt_ind")
    private String stmtInd;

    @Column(name = "stmt_type")
    private String stmtType;

    @Column(name = "stmt_date")
    private String stmtDate;

    @Column(name = "fee_amt")
    private BigDecimal FeeAmt;

    @Column(name = "status")
    private String status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

}
