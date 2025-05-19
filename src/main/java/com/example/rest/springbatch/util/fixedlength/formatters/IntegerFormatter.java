package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.util.fixedlength.annotation.FixedField;

public class IntegerFormatter extends Formatter<Integer> {

        @Override
        public Integer asObject(String string, FixedField field){
            return Integer.parseInt(string);
        }

        @Override
        public String asString(Integer object, FixedField field) {
            return object.toString();
        }
        }
