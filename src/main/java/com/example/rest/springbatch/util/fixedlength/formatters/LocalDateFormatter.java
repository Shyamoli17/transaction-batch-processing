package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LocalDateFormatter extends Formatter<LocalDate> {
    private static final String DEFAULT_FORMAT = "yyyyMMdd";

    private static DateTimeFormatter format(FixedField field){
        return DateTimeFormatter.ofPattern(!field.format().isEmpty() ? field.format() : DEFAULT_FORMAT);
    }
    @Override
    public LocalDate asObject(String string, FixedField field){
            return LocalDate.parse(string, format(field));
    }

    @Override
    public String asString(LocalDate object, FixedField field){
        return object.format(format(field));
    }
}
