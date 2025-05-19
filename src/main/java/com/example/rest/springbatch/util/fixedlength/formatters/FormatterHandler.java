package com.example.rest.springbatch.util.fixedlength.formatters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class FormatterHandler {

    private static final Formatter<String> stringFormatter = new StringFormatter();
    private static final Formatter<Short> shortFormatter = new ShortFormatter();
    private static final Formatter<Integer> integerFormatter = new IntegerFormatter();
    private static final Formatter<Long> longFormatter = new LongFormatter();
    private static final Formatter<Date> dateFormatter = new DateFormatter();
    private static final Formatter<LocalDate> localDateFormatter = new LocalDateFormatter();
    private static final Formatter<LocalTime> localTimeFormatter = new LocalTimeFormatter();
    private static final Formatter<LocalDateTime> localDateTimeFormatter = new LocalDateTimeFormatter();
    private static final Formatter<BigDecimal> bigDecimalFormatter = new BigDecimalFormatter();

    public static Formatter<?> getFormatter(final Class<?> type) throws FixedLengthException {
        if (String.class.equals(type)) {
            return stringFormatter;
        } else if (BigDecimal.class.equals(type)) {
            return bigDecimalFormatter;
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            return longFormatter;
        } else if (LocalDate.class.equals(type)) {
            return localDateFormatter;
        } else if (LocalDateTime.class.equals(type)) {
            return localDateTimeFormatter;
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return integerFormatter;
        } else if (Date.class.equals(type)) {
            return dateFormatter;
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            return shortFormatter;
        } else if (LocalTime.class.equals(type)) {
            return localTimeFormatter;
        } else {
            throw new FixedLengthException("No formatter found for type: " + type.getName());
        }
    }
    public static Formatter<String> getStringFormatter() {
        return stringFormatter;
    }

    public static Formatter<Short> getShortFormatter() {
        return shortFormatter;
    }

    public static Formatter<Integer> getIntegerFormatter() {
        return integerFormatter;
    }

    public static Formatter<Long> getLongFormatter() {
        return longFormatter;
    }

    public static Formatter<Date> getDateFormatter() {
        return dateFormatter;
    }

    public static Formatter<LocalDate> getLocalDateFormatter() {
        return localDateFormatter;
    }

    public static Formatter<LocalTime> getLocalTimeFormatter() {
        return localTimeFormatter;
    }

    public static Formatter<LocalDateTime> getLocalDateTimeFormatter() {
        return localDateTimeFormatter;
    }

    public static Formatter<BigDecimal> getBigDecimalFormatter() {
        return bigDecimalFormatter;
    }

}
