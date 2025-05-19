package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

public class StringFormatter extends Formatter<String> {

    @Override
    public String asObject(String string, FixedField field){
        return string;
    }

    @Override
    public String asString(String object, FixedField field){
        return object;
    }
}
