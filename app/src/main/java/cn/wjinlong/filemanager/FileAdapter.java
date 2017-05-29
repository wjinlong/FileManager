package cn.wjinlong.filemanager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by King on 2017/5/28 0028.
 */

public class FileAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String,String>> data;

    public FileAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (convertView==null) {
            view = View.inflate(context, R.layout.list_row, null);
        }
        TextView fileName = (TextView) view.findViewById(R.id.file_name);
        TextView path = (TextView) view.findViewById(R.id.time);
        ImageView image = (ImageView) view.findViewById(R.id.icon);

        HashMap<String,String> map;
        map=data.get(i);

        fileName.setText(map.get("fileName"));
        path.setText(map.get("path"));

        String extension = map.get("extension");
        String filetype=fileType(extension);
        int resourseID = getResourseID(filetype);
        image.setImageResource(resourseID);

        return view;
    }

    private int getResourseID(String filetype) {
        int defaltResourseID = R.drawable.filetype_blank;
        int resourseID;

        if (filetype.equals("folder")){
            resourseID=R.drawable.filetype_folder;
        }else if (filetype.equals("image")){
            resourseID=R.drawable.filetype_image;
        }else if (filetype.equals("package")){
            resourseID=R.drawable.filetype_package;
        }else if (filetype.equals("sheet")){
            resourseID=R.drawable.filetype_sheet;
        }else if (filetype.equals("sound")){
            resourseID=R.drawable.filetype_sound;
        }else if (filetype.equals("text")){
            resourseID=R.drawable.filetype_text;
        }else if (filetype.equals("video")){
            resourseID=R.drawable.filetype_video;
        }else {
            resourseID=defaltResourseID;
        }
        return resourseID;
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
