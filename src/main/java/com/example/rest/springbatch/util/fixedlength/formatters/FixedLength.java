package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;
import com.example.rest.springbatch.util.fixedlength.annotation.FixedLine;
import com.example.rest.springbatch.util.fixedlength.annotation.SplitLineAfter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class FixedLength<T> {

    private FixedFormatLine<? extends T> lineType = null;

    private FixedFormatLine<T> classToLineDesc(final Class<? extends T> clazz) {
        FixedFormatLine<T> fixedFormatLine = new FixedFormatLine<>();
        fixedFormatLine.clazz = clazz;

        FixedLine annotation = clazz.getDeclaredAnnotation(FixedLine.class);
        if (annotation != null) {
            fixedFormatLine.startsWith = annotation.startsWith();
        }

        for (Field field : clazz.getDeclaredFields()) {
            FixedField fieldAnnotation = field.getDeclaredAnnotation(FixedField.class);
            if (fieldAnnotation == null) {
                continue;
            }
            FixedFormatLine.FixedFormatField fixedFormatField = new FixedFormatLine.FixedFormatField(field, fieldAnnotation);
            fixedFormatLine.fixedFormatFields.add(fixedFormatField);
            fixedFormatLine.fixedFormatFieldsMap.put(field.getName(), fixedFormatField);
        }

        for (Method method : clazz.getMethods()) {
            SplitLineAfter splitLineAfter = method.getDeclaredAnnotation(SplitLineAfter.class);
            if (splitLineAfter != null) {
                fixedFormatLine.splitAfterMethod = method;
                break;
            }
        }

        return fixedFormatLine;
    }

    public FixedLength<T> registerLineType(final Class<? extends T> lineClass) {
        this.lineType = classToLineDesc(lineClass);
        return this;
    }

    private T lineToObject(String line) {
        Class<? extends T> clazz = lineType.clazz;
        T lineAsObject;

        try {
            lineAsObject = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new FixedLengthException("No empty constructor in class: " + clazz.getName(), e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new FixedLengthException("Unable to instantiate " + clazz.getName(), e);
        }

        for (FixedFormatLine.FixedFormatField fixedFormatField : lineType.fixedFormatFields) {
            FixedField fieldAnnotation = fixedFormatField.getFixedFieldAnnotation();
            Field field = fixedFormatField.getField();

            int startOfFieldIndex = fieldAnnotation.offset() - 1;
            int endOfFieldIndex = startOfFieldIndex + fieldAnnotation.length();

            if (endOfFieldIndex > line.length()) {
                continue;
            }

            String str = fieldAnnotation.align().remove(line.substring(startOfFieldIndex, endOfFieldIndex),
                    fieldAnnotation.padding());

            if (!acceptFieldContent(str, fieldAnnotation)) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = FormatterHandler.getFormatter(field.getType())
                        .asObject(str, fieldAnnotation);
                field.set(lineAsObject, value);
            } catch (IllegalAccessException e) {
                throw new FixedLengthException("Access to field (" + field.getName() + ") failed", e);
            } catch (RuntimeException re) {
                throw new FixedLengthException("Unable to read the field(" + field.getName() + ")", re);
            }
        }
        return lineAsObject;
    }

    private boolean acceptFieldContent(String content, FixedField fieldAnnotation) {
        if (content == null) {
            return false;
        }
        if (content.trim().isEmpty()) {
            return false;
        }
        if (fieldAnnotation.ignore().isEmpty()) {
            return true;
        }
        Pattern pattern = Pattern.compile(fieldAnnotation.ignore());
        return !pattern.matcher(content).matches();
    }

    private FixedFormatLine.FixedFormatFieldValue readValue(String line, String fieldName) throws FixedLengthException {

        FixedFormatLine.FixedFormatField fixedFormatField = lineType.fixedFormatFieldsMap.get(fieldName);
        if (fixedFormatField == null)
            throw new FixedLengthException("Unable to find the field (" + fieldName + ")");

        FixedField fieldAnnotation = fixedFormatField.getFixedFieldAnnotation();

        int startOfFieldIndex = fieldAnnotation.offset() - 1;
        int endofFieldIndex = startOfFieldIndex + fieldAnnotation.length();
        if (endofFieldIndex > line.length()) {
            return null;
        }
        String str = fieldAnnotation.align().remove(line.substring(startOfFieldIndex, endofFieldIndex)
                ,fieldAnnotation.padding());
        if (!acceptFieldContent(str, fieldAnnotation)) {
            return null;
        }
        return new FixedFormatLine.FixedFormatFieldValue(str, fieldAnnotation);
    }

    public String readString(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getStringFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public BigDecimal readBigDecimal(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getBigDecimalFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public Short readShort(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getShortFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public Integer readInteger(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getIntegerFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public Long readLong(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getLongFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public Date readDate(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getDateFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public LocalDate readLocalDate(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getLocalDateFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public LocalTime readLocalTime(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getLocalTimeFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

    public LocalDateTime readLocalDateTime(String line, String fieldName) throws FixedLengthException {
        FixedFormatLine.FixedFormatFieldValue ffValue = this.readValue(line, fieldName);
        if (ffValue == null) {
            return null;
        }
        return FormatterHandler.getLocalDateTimeFormatter().asObject(
                ffValue.getValue(),
                ffValue.getFixedFieldAnnotation()
        );
    }

//    public T parse(String line) throws FixedLengthException{
//        return this.lineToObject(line);
//    }
public T parse(String line) throws FixedLengthException {
    if (this.lineType == null) {
        throw new FixedLengthException("No line type registered. Call registerLineType(Class<?>) before parsing.");
    }
    return this.lineToObject(line);
}

    @SuppressWarnings("unchecked")
    public String format(T line) {
        final StringBuilder builder = new StringBuilder();

        Arrays.stream(line.getClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(FixedField.class) != null)
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(FixedField.class).offset()))
                .forEach(f -> {
                    FixedField fixedFieldAnnotation = f.getAnnotation(FixedField.class);
                    Formatter<T> formatter = (Formatter<T>) FormatterHandler.getFormatter(f.getType());
                    f.setAccessible(true);
                    T value;
                    try {
                        value = (T) f.get(line);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new FixedLengthException(e.getMessage());
                    }

                    if (value != null) {
                        builder.append(
                                fixedFieldAnnotation.align().make(
                                        formatter.asString(value, fixedFieldAnnotation),
                                        fixedFieldAnnotation.length(),
                                        fixedFieldAnnotation.padding()
                                )
                        );
                    }
                });

        return builder.toString();
    }

    @SuppressWarnings("unused")
    private final class FixedFormatRecord{
        private final String rawLine;
        private final FixedFormatLine<? extends T> fixedFormatLine;

        private FixedFormatRecord(final String rawLine, final FixedFormatLine<? extends T> fixedFormatLine){
            this.rawLine = rawLine;
            this.fixedFormatLine = fixedFormatLine;
        }
    }
    @SuppressWarnings("unused")
    private static class FixedFormatLine<T> {
        private String startsWith = null;
        private Class<? extends T> clazz;
        private final List<FixedFormatField> fixedFormatFields = new ArrayList<>();
        private final Map<String, FixedFormatField> fixedFormatFieldsMap = new HashMap<>();
        private Method splitAfterMethod;

        public String getStartsWith() {
            return startsWith;
        }

        public void setStartsWith(String startsWith) {
            this.startsWith = startsWith;
        }

        public Class<? extends T> getClazz() {
            return clazz;
        }

        public void setClazz(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        private static final class FixedFormatField {
            private final Field field;
            private final FixedField fixedFieldAnnotation;

            private FixedFormatField(Field field, FixedField fixedField) {
                this.field = field;
                this.fixedFieldAnnotation = fixedField;
            }

            public Field getField() {
                return field;
            }

            public FixedField getFixedFieldAnnotation() {
                return fixedFieldAnnotation;
            }
        }

        private static final class FixedFormatFieldValue {
            private final String value;
            private final FixedField fixedFieldAnnotation;

            private FixedFormatFieldValue(String value, FixedField fixedField) {
                this.value = value;
                this.fixedFieldAnnotation = fixedField;
            }

            public String getValue() {
                return value;
            }

            public FixedField getFixedFieldAnnotation() {
                return fixedFieldAnnotation;
            }
        }
    }


}

