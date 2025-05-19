package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

public abstract class Formatter<T> {

    public T parse(String value, FixedField field){
        T result = null;
        if(value != null){
            result = asObject(value, field);
        }
        return result;
    }

    public abstract T asObject(String string, FixedField field);

    public abstract String asString(T object, FixedField field);
}
