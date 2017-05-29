package cn.wjinlong.filemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.wjinlong.filemanager.entity.FileInfo;
import cn.wjinlong.filemanager.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<HashMap<String,String>> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= (ListView) findViewById(R.id.list_item);

        FileUtil fileUtil = new FileUtil();
        FileInfo[] fileInfos = fileUtil.readFileInfo();


        for (int i = 0; i < fileInfos.length; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("fileName",fileInfos[i].getFileName());
            map.put("path", fileInfos[i].getPath());
            map.put("extension",fileInfos[i].getExtension());
            list.add(map);
        }

        listView.setAdapter(new FileAdapter(this,list));

    }
}
