package com.example.rest.springbatch.util.fixedlength.annotation;

import com.example.rest.springbatch.util.fixedlength.formatters.Align;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to define properties for fixed-length file fields.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedField {

    /**
     * Start offset (zero-based) of the field in the fixed-length line.
     */
    int offset();

    /**
     * Length of the field (number of characters).
     */
    int length();

    /**
     * Alignment direction for the field.
     * LEFT for left-aligned, RIGHT for right-aligned.
     */
    Align align() default Align.RIGHT;

    /**
     * Padding character used to fill extra space.
     */
    char padding() default ' ';

    /**
     * Optional format string (e.g., for date or decimal formatting).
     */
    String format() default "";

    /**
     * A divisor used for converting integer values to decimal (e.g., divide by 100).
     */
    int divide() default 1;

    /**
     * Whether this field should be ignored during processing.
     */
    String ignore() default "";
}
