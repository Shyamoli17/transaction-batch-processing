
package com.example.rest.springbatch.util.fixedlength.formatters;

import com.example.rest.springbatch.model.item.LineDetailBaseItem;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class FixedLengthLineMapper<T extends LineDetailBaseItem> implements LineMapper<T>, InitializingBean {

    private FixedLength<T> fixedlength;

    /**
     * Sets the FixedLength parser to use for mapping lines.
     */
    public void setFixedlength(FixedLength<T> fixedlength) {
        this.fixedlength = fixedlength;
    }

    @Override
    public T mapLine(String line, int lineNumber) throws Exception {
        if (fixedlength == null) {
            throw new IllegalStateException("FixedLength must be set before calling mapLine.");
        }
        T item = fixedlength.parse(line);
        // Set the raw line and line number if the item supports it
        if (item instanceof LineDetailBaseItem) {
            ((LineDetailBaseItem) item).setFileLine(line);
            ((LineDetailBaseItem) item).setFileLineNumber(lineNumber);
        }
        return item;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(fixedlength, "The FixedLength must be set");
    }
}
