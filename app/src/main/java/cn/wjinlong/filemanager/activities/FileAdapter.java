package cn.wjinlong.filemanager.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import cn.wjinlong.filemanager.R;

/**
 * Created by King on 2017/5/28 0028.
 */

public class FileAdapter extends BaseAdapter {
    Context context;
    List<File> list;

    public FileAdapter() {
    }

    public FileAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        File file = list.get(i);
        View view;
        ViewHolder viewHolder;

        if (convertView==null){
            view = LayoutInflater.from(context).inflate(R.layout.file_item, null);

            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) view.findViewById(R.id.icon);
            viewHolder.fileName = (TextView) view.findViewById(R.id.file_name);
            viewHolder.size = (TextView) view.findViewById(R.id.size);
            viewHolder.time = (TextView) view.findViewById(R.id.time);

            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }



        String fileName = file.getName();
        String extension;
        String size;
        if (file.isDirectory()){
            extension = "folder";
            size = "文件夹";
        }else {
            extension = fileName.substring(fileName.lastIndexOf(".")+1);
            long fileLength = file.length();
            size = fileSize(fileLength);
        }

        String time = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(file.lastModified());
        String fileType = fileType(extension);
        int resourceID = getResourceID(fileType);

        viewHolder.image.setImageResource(resourceID);
        viewHolder.fileName.setText(fileName);
        viewHolder.size.setText(size);
        viewHolder.time.setText(time);

        updateBackground(i, view);

        return view;
    }

    public void updateBackground(int i, View view) {
        int backgroundID;
        ListView listView = null;
        if (context instanceof MainActivity){
            listView = ((MainActivity) context).listView;
        } else if (context instanceof ChoosePathActivity) {
            listView = ((ChoosePathActivity) context).listView;
        }

        if (listView.isItemChecked(i)){
            backgroundID = R.drawable.gradient_bg_hover;
        }else {
            backgroundID = R.drawable.gradient_bg;
        }
        Drawable background = (Drawable) context.getResources().getDrawable(backgroundID);
        view.setBackground(background);
    }

    public String fileSize(long fileLength) {
        int level=1;
        String unit;

        double length = (double) fileLength;
        while (length>1024){
            level++;
            length = length / 1024;
        }

        switch (level){
            case 1:
                unit = "B";
                break;
            case 2:
                unit ="K";
                break;
            case 3:
                unit = "M";
                break;
            case 4:
                unit = "G";
                break;
            default:
                unit = "B";
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(length)+unit;
    }


    private class ViewHolder{
        ImageView image;
        TextView fileName;
        TextView size;
        TextView time;
    }


    private int getResourceID(String filetype) {
        int defaultResourceID = R.drawable.filetype_blank;
        int resourceID;

        switch (filetype) {
            case "folder":
                resourceID = R.drawable.filetype_folder;
                break;
            case "image":
                resourceID = R.drawable.filetype_image;
                break;
            case "package":
                resourceID = R.drawable.filetype_package;
                break;
            case "sheet":
                resourceID = R.drawable.filetype_sheet;
                break;
            case "sound":
                resourceID = R.drawable.filetype_sound;
                break;
            case "text":
                resourceID = R.drawable.filetype_text;
                break;
            case "video":
                resourceID = R.drawable.filetype_video;
                break;
            default:
                resourceID = defaultResourceID;
                break;
        }
        return resourceID;
    }

    private String fileType(String extension) {
        String defaultFiletype = "blank";
        String filetype;

        HashSet<String> imageExtension = new HashSet<>();
        imageExtension.add("img");
        imageExtension.add("ico");
        imageExtension.add("png");

        HashSet<String> packageExtension = new HashSet<>();
        packageExtension.add("apk");
        packageExtension.add("zip");
        packageExtension.add("rar");

        HashSet<String> soundExtension = new HashSet<>();
        soundExtension.add("mp3");

        HashSet<String> sheetExtension = new HashSet<>();
        sheetExtension.add("xls");
        sheetExtension.add("xlsx");

        HashSet<String> textExtension = new HashSet<>();
        textExtension.add("txt");

        HashSet<String> videoExtension = new HashSet<>();
        videoExtension.add("mp4");

        if (extension.equals("folder")){
            filetype="folder";
        }else if (imageExtension.contains(extension)){
            filetype="image";
        }else if (packageExtension.contains(extension)){
            filetype="package";
        }else if (sheetExtension.contains(extension)){
            filetype="sheet";
        }else if (soundExtension.contains(extension)){
            filetype="sound";
        }else if (textExtension.contains(extension)){
            filetype="text";
        }else if (videoExtension.contains(extension)){
            filetype="video";
        }else{
            filetype=defaultFiletype;
        }
        return filetype;
    }
}
