package com.example.rest.springbatch.model.item;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;
import com.example.rest.springbatch.util.fixedlength.formatters.Align;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data@EqualsAndHashCode(callSuper = true)
public class StatementFileInput extends LineDetailBaseItem{

    @NotBlank(message = "Record Type field cannot be empty")
    @NotNull(message = "Record Type field cannot be null")
    @FixedField(offset = 1, length = 2, align = Align.LEFT)
    private String recordType;

    @NotBlank(message = "Account no field cannot be empty")
    @NotNull(message = "Account no field cannot be null")
    @FixedField(offset = 3, length = 14, align = Align.LEFT, padding = ' ')
    private String accountNo;

    @FixedField(offset = 17, length = 1, align = Align.LEFT)
    private String indicator;

    @NotBlank(message = "Stmt Type field cannot be empty")
    @NotNull(message = "Stmt Type field cannot be null")
    @FixedField(offset = 18, length = 1, align = Align.LEFT)
    private String StmtType;

    @FixedField(offset = 19, length = 8, align = Align.LEFT)
    private String StmtDate;

    @FixedField(offset = 27, length = 8, align = Align.LEFT, padding = ' ')
    private String userId;

    @NotBlank(message = "Product code field cannot be empty")
    @NotNull(message = "Product code field cannot be null")
    @FixedField(offset = 35, length = 3, align = Align.LEFT)
    private String productCode;
}
