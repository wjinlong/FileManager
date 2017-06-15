package cn.wjinlong.filemanager.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wjinlong.filemanager.activities.MainActivity;

/**
 * Created by King on 2017/5/27 0027.
 */

public class FileUtil {
    private String path;
    private List<File> fileList;

    private final String[][] MIME_MapTable={
            //{后缀名， MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",    "image/bmp"},
            {".c",  "text/plain"},
            {".class",  "application/octet-stream"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",   "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h",  "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".mpga",   "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop",   "text/plain"},
            {".rc", "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh", "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".z",  "application/x-compress"},
            {".zip",    "application/x-zip-compressed"},
            {"",        "*/*"}
    };

    public FileUtil() {
        path = Environment.getExternalStorageDirectory().getPath();
    }

    public FileUtil(String path) {
        this.path = path;
    }

    public List<File> getFileList() {
        File file = new File(path);
        fileList = new ArrayList<>();

        if (file.exists() && file.isDirectory()){
            String[] files = file.list();
            boolean startWithPoint = false;

            for (String i : files) {
                File tmp = new File(path+File.separator+i);
                startWithPoint = tmp.getName().charAt(0) == '.';
                if (tmp.exists() && MainActivity.SHOW_HIDE_FILES || !startWithPoint){
                    fileList.add(tmp);
                }
            }
        }
        return fileList;
    }

    public List<File> getFileList(int a) {//只返回文件夹
        File file = new File(path);
        fileList = new ArrayList<>();
        if (file.exists() && file.isDirectory()){
            String[] files = file.list();
            for (String i : files) {
                File tmp = new File(path+File.separator+i);
                if (tmp.exists()&& tmp.isDirectory()){
                    fileList.add(tmp);
                }
            }
        }
        return fileList;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtension(){
        File file = new File(path);
        String extension = "";
        if (file.exists() && file.isFile()){
            String fileName = file.getName();
            extension = fileName.substring(fileName.lastIndexOf(".")+1);
        }
        return extension;
    }

    public String getMIMEType(){
        String extension = getExtension();
        String type="*/*";
        if (extension.equals("")){
            return type;
        }
        extension = "." + extension;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<MIME_MapTable.length;i++){
            if(extension.toLowerCase().equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }


    /**
     * 复制文件
     * 从源路径到目标文件夹路径，文件名保持一致
     * 如果目标文件夹不存在则自动创建
     * 如果文件已经存在则自动编号-n
     *
     * @param srcFile   源文件绝对路径
     * @param dstDir    目标文件夹绝对路径
     * @return  是否成功复制文件
     */
    public static boolean copyFile(File srcFile, File dstDir) {
        if (!srcFile.exists() || srcFile.isDirectory()) {
            return false;
        }
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }

        String oldFileName = srcFile.getName();
        Pattern extensionPattern = Pattern.compile("\\.\\w+");
        Matcher matcher = extensionPattern.matcher(oldFileName);

        String nameBody;
        String extension;
        if (matcher.find()) {
            nameBody = oldFileName.substring(0, matcher.start());
            extension = oldFileName.substring(matcher.start());
        } else {
            nameBody = oldFileName;
            extension = "";
        }

        int fileNumber = 0;
        File newFile = new File(dstDir, oldFileName);
        while (newFile.exists()) {
            fileNumber++;
            String newFileName = nameBody + "-" + fileNumber + extension;
            newFile = new File(dstDir, newFileName);
        }

        try {
            FileChannel fileIn = new FileInputStream(srcFile).getChannel();
            FileChannel fileOut = new FileOutputStream(newFile).getChannel();
            fileIn.transferTo(0, fileIn.size(), fileOut);
            fileIn.close();
            fileOut.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 复制文件或文件夹
     * 如果目标文件夹不存在则自动创建
     * 如果文件或文件夹已经存在则自动添加编号-n
     *
     * @param src   源文件或文件夹绝对路径
     * @param dstDir    目标文件夹绝对路径
     * @return  是否成功复制文件或文件夹
     */
    public static boolean copy(File src, File dstDir){
        if (!src.exists()) {
            Log.d("源文件不存在",src.getName());
            return false;
        }
        if (!dstDir.exists()) {
            Log.d("目标目录不存在",src.getName());
            dstDir.mkdirs();
        }
        if (src.isFile()) {// 文件
            Log.d("复制文件",src.getName());
            copyFile(src, dstDir);
        } else {// 文件夹
            Log.d("复制文件夹",src.getName());
            String oldSrcName = src.getName();
            int srcNumber = 0;
            File newSrcDir = new File(dstDir, oldSrcName);
            while (newSrcDir.exists()) {
                srcNumber++;
                String newSrcName = oldSrcName + "-" + srcNumber;
                newSrcDir = new File(dstDir, newSrcName);
            }
            newSrcDir.mkdirs();
            for (File srcSub : src.listFiles()) {
                // 递归复制源文件夹下子文件和文件夹
                copy(srcSub, newSrcDir);
            }
        }
        return true;
    }


    /**
     * 移动文件
     *
     * @param srcFile   源文件绝对路径
     * @param dstDir    目标文件夹绝对路径
     * @return  是否成功移动文件
     */
    public static boolean moveFile(File srcFile, File dstDir) {
        if (!srcFile.exists() || srcFile.isDirectory()) {
            return false;
        }
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        String oldFileName = srcFile.getName();
        File dstFile = new File(dstDir, oldFileName);

        if (srcFile.renameTo(dstFile)) {//直接重命名绝对路径速度更快
            return true;
        } else {//文件已经存在，需要自动编号复制再删除源文件
            copyFile(srcFile, dstDir);
            srcFile.delete();
        }
        return true;
    }

    /**
     * 移动文件或文件夹
     * 如果目标文件夹不存在则自动创建
     * 如果文件或文件夹已经存在则自动编号-n
     *
     * @param src    源文件或文件夹绝对路径
     * @param dstDir 目标文件夹绝对路径
     * @return 是否成功移动文件或文件夹
     */
    public static boolean move(File src, File dstDir) {
        if (!src.exists()) {
            return false;
        }
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        if (src.isFile()) {//文件
            moveFile(src, dstDir);
        } else {//文件夹
            String oldSrcName = src.getName();
            int srcNumber = 0;
            File newSrcDir = new File(dstDir, oldSrcName);
            while (newSrcDir.exists()) {
                srcNumber++;
                String newSrcName = oldSrcName + "-" + srcNumber;
                newSrcDir = new File(dstDir, newSrcName);
            }
            newSrcDir.mkdirs();
            for (File srcSub : src.listFiles()) {
                //递归移动源文件夹下子文件和文件夹
                move(srcSub, newSrcDir);
            }
            src.delete();
        }
        return true;
    }


    /**
     * 删除文件或文件夹
     *
     * @param src 源文件或文件夹绝对路径
     * @return 是否成功删除文件或文件夹
     */
    public static boolean delete(File src) {
        if (!src.exists()) {
            return false;
        }
        if (src.isFile()) {
            src.delete();
        } else {
            for (File srcSub : src.listFiles()) {
                delete(srcSub);// 递归删除源文件夹下子文件和文件夹
            }
            src.delete();
        }
        return true;
    }
}
