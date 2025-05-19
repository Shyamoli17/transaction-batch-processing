package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

public class ShortFormatter extends Formatter<Short> {

    @Override
    public Short asObject(String string, FixedField field){
        return Short.parseShort(string);
    }

    @Override
    public String asString(Short object, FixedField field){
        return object.toString();
    }
}
