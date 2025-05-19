package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalFormatter extends Formatter<BigDecimal> {

    @Override
    public BigDecimal asObject(String string, FixedField field){
        BigDecimal result = new BigDecimal("".equals(string) ? "0": string);
        if (field.divide() != 0 && result.compareTo(BigDecimal.ZERO) != 0){
            result = result.divide(
                    BigDecimal.TEN.pow(field.divide()),
                    field.divide(),
                    RoundingMode.HALF_UP);
        }
        return result;
    }

    @Override
    public String asString(BigDecimal object, FixedField field){
        if(object == null){
            return "0";
        }
        BigDecimal result = object;
        if (field.divide() != 0){
            result = object.multiply((BigDecimal.TEN.pow(field.divide())));
        }
        return result.setScale(0).toPlainString();
    }
}
