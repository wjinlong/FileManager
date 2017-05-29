package cn.wjinlong.filemanager.entity;

import android.media.Image;

/**
 * Created by King on 2017/5/27 0027.
 */

public class FileInfo {
    private String fileName;
    private String time;
    private Image image;
    private String path;
    private String extension;

    public FileInfo() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
