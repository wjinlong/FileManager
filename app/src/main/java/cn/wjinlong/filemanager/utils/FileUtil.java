package cn.wjinlong.filemanager.utils;

import android.os.Environment;

import java.io.File;

import cn.wjinlong.filemanager.entity.FileInfo;

/**
 * Created by King on 2017/5/27 0027.
 */

public class FileUtil {

    private String path;

    FileInfo[] fileInfos;
    public FileUtil() {
        path= Environment.getExternalStorageDirectory().getPath();
    }

    public FileUtil(String path) {
        this.path=path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileInfo[] readFileInfo(){
        File file = new File(path);
        if (file.exists() && file.isDirectory()){
            String[] fileList = file.list();
            fileInfos = new FileInfo[fileList.length];

            for (int i = 0; i < fileList.length; i++) {
                File tmp = new File(path+File.separator+fileList[i]);
                fileInfos[i] = new FileInfo();
                if (tmp.isDirectory()){
                    fileInfos[i].setPath(tmp.getAbsolutePath());
                    fileInfos[i].setFileName(tmp.getName());
                    fileInfos[i].setExtension("folder");

                }else{
                    fileInfos[i].setPath(tmp.getAbsolutePath());
                    fileInfos[i].setFileName(tmp.getName());
                    fileInfos[i].setExtension(tmp.getName().substring(tmp.getName().lastIndexOf(".")+1));
                }
            }
        }
        return fileInfos;
    }

}
