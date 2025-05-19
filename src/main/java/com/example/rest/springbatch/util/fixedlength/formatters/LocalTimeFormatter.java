package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeFormatter extends Formatter<LocalTime> {
    private static final String DEFAULT_FORMAT = "HHmmss";

    private static DateTimeFormatter format(FixedField field){
        return DateTimeFormatter.ofPattern(!field.format().isEmpty() ? field.format() : DEFAULT_FORMAT);
    }
    @Override
    public LocalTime asObject(String string, FixedField field){
        return LocalTime.parse(string, format(field));
    }

    @Override
    public String asString(LocalTime object, FixedField field){
        return object.format(format(field));
    }
}
