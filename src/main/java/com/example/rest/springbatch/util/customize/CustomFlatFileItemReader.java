package com.example.rest.springbatch.util.customize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.ExecutionContext;
//import org.springframework.batch.item.file.ResourceLineReader;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.core.io.Resource;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomFlatFileItemReader<T> extends AbstractItemCountingItemStreamItemReader<T>
        implements ResourceAwareItemReaderItemStream<T> {

    private BufferedReader reader;
    private Resource resource;
    private LineMapper<T> lineMapper;
    private boolean noInput;
    private BufferedReaderFactory bufferedReaderFactory = new DefaultBufferedReaderFactory();
    public static final String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();
    private String encoding = DEFAULT_CHARSET;
    public static final String[] DEFAULT_COMMENT_PREFIXES = new String[] { "#" };
    private String[] comments = DEFAULT_COMMENT_PREFIXES;
    private LineCallbackHandler skippedLinesCallback;

    public CustomFlatFileItemReader() {
        setName("customFlatFileItemReader");
    }

    public void setLineMapper(LineMapper<T> lineMapper) {
        this.lineMapper = lineMapper;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    protected T doRead() throws Exception {
        if (noInput) {
            return null;
        }
        if (reader == null) {
            throw new IllegalStateException("Reader is not initialized. Did you call open()?");
        }
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                return null;
            }
            // Skip lines that start with any comment prefix
            for (String prefix : comments) {
                if (line.startsWith(prefix)) {
                    if (skippedLinesCallback != null) {
                        skippedLinesCallback.handleLine(line);
                    }
                    line = null; // force loop to continue
                    break;
                }
            }
        } while (line == null);

        if (lineMapper == null) {
            throw new IllegalStateException("LineMapper is not set. Did you call setLineMapper()?");
        }
        try {
            return lineMapper.mapLine(line, getCurrentItemCount());
        } catch (Exception e) {
            throw new FlatFileParseException("Parsing error at line: " + getCurrentItemCount(), line);
        }
    }

    @Override
    protected void doOpen() throws Exception {
        if (resource == null || !resource.exists()) {
            noInput = true;
            return;
        }

        reader = bufferedReaderFactory.create(resource, "UTF-8");
        noInput = false;
    }

    @Override
    protected void doClose() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }

    @Override
    protected void jumpToItem(int itemIndex) throws Exception {
        for (int i = 0; i < itemIndex; i++) {
            reader.readLine();
        }
    }

    public void setEncoding(String encoding){
        this.encoding = encoding;
    }

    public void setComments(String[] comments) {
        this.comments = new String[comments.length];
        System.arraycopy(comments, 0, this.comments, 0, comments.length);
    }

    public void setSkippedLinesCallback(LineCallbackHandler skippedLinesCallback) {
        this.skippedLinesCallback = skippedLinesCallback;
    }
}
