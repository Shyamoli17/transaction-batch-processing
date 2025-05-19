package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeFormatter extends Formatter<LocalDateTime> {
    private static final String DEFAULT_FORMAT = "MMddyyyy HHmmss";

    private static DateTimeFormatter format(FixedField field){
        return DateTimeFormatter.ofPattern(!field.format().isEmpty() ? field.format() : DEFAULT_FORMAT);
    }
    @Override
    public LocalDateTime asObject(String string, FixedField field){
        return LocalDateTime.parse(string, format(field));
    }

    @Override
    public String asString(LocalDateTime object, FixedField field){
        return object.format(format(field));
    }
}
