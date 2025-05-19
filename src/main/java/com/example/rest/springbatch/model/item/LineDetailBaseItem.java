package com.example.rest.springbatch.model.item;

import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.Resource;

public abstract class LineDetailBaseItem implements ResourceAware {
    private String fileName;
    private String fileLine;
    private int fileLineNumber;
    private Resource resource;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLine() {
        return fileLine;
    }

    public void setFileLine(String fileLine) {
        this.fileLine = fileLine;
    }

    public int getFileLineNumber() {
        return fileLineNumber;
    }

    public void setFileLineNumber(int fileLineNumber) {
        this.fileLineNumber = fileLineNumber;
    }

    public Resource getResource() {
        return resource;
    }

//    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
