package com.example.rest.springbatch.computeStatementFeeJob;

import com.example.rest.springbatch.model.entity.TblStatementEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

@Slf4j
public class computeStatementFeeJobItemProcessor implements ItemProcessor<TblStatementEntity, TblStatementEntity> {

    @Override
    public TblStatementEntity process(TblStatementEntity item) {
        if (item == null) {
            return null;
        }

        String stmtType = item.getStmtType();
        String productCode = item.getProductCode();
        String stmtInd = item.getStmtInd();

        double feeAmt;
        if ("N".equalsIgnoreCase(stmtType)
                && "020".equals(productCode)
                && "2".equals(stmtInd)) {
            feeAmt = 25.0;
        } else {
            feeAmt = 40.0;
        }

        // Assuming setFeeAmt(Double) exists in TblStatementEntity
        item.setFeeAmt(BigDecimal.valueOf(feeAmt));
        log.debug("Computed fee_amt={} for statement id={} with stmt_type={}, product_code={}, stmt_ind={}",
                feeAmt, item.getId(), stmtType, productCode, stmtInd);
        return item;
    }
}