package com.example.rest.springbatch.util.fixedlength.formatters;

public class FixedLengthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FixedLengthException(){
        super();
    }
    public FixedLengthException(String message) {
        super(message);
    }
    public FixedLengthException(String message, Throwable cause){
        super(message, cause);
    }
}
