package org.realitix.dfilesearch.webservice.beans;

public class FileResponse {
    private String hash;
    private int fileSize;

    public FileResponse(FileResponseBuilder builder) {
        this.hash = builder.hash;
        this.fileSize = builder.fileSize;
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

    public static class FileResponseBuilder {
        private String hash;
        private int fileSize;

        public String getHash() {
            return hash;
        }

        public static FileResponseBuilder newInstance() {return new FileResponseBuilder();}

        public FileResponseBuilder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public int getFileSize() {
            return fileSize;
        }

        public FileResponseBuilder setFileSize(int fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public FileResponse build() {return new FileResponse(this);}
    }
}
