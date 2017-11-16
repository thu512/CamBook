package com.mustdo.cambook.Model;

/**
 * Created by lcj on 2017. 11. 16..
 */

public class DownloadUrl {
    String url;
    String fileName;

    public DownloadUrl() {
    }

    @Override
    public String toString() {
        return "DownloadUrl{" +
                "url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DownloadUrl(String url, String fileName) {
        this.url = url;

        this.fileName = fileName;
    }
}
