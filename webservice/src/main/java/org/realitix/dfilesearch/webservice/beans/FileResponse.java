package org.realitix.dfilesearch.webservice.beans;

public class FileResponse {
    private String hash;
    private int fileSize;

    public FileResponse() {
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
