package com.example.rest.springbatch.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConstants {
    public static final String HEADER_TYPE_H = "H";
    public static final String TRAILER_TYPE_T = "T";
    public static final String FILE_TYPE = "STMT_FILE";
    public static final String DELIMETER_PIPE = "|";
    
    public static final String SERVICE_TYPE_ST = "STMT";
    
    //---tables
    public static final String BATCH_ERROR_TBL = "batch_error_tbl";
    public static final String STATUS_INVALID = "INVALID";
    
    public static final String TABLE_STATEMENT = "tbl_statement";
}
