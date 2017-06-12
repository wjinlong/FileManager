package cn.wjinlong.filemanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.wjinlong.filemanager.R;
import cn.wjinlong.filemanager.utils.FileUtil;

/**
 * Created by King on 2017/6/12 0012.
 */

public class ChoosePathActivity extends Activity {
    ListView listView;
    List<File> fileList;
    String rootPath = Environment.getExternalStorageDirectory().getPath();//根目录
    String currentPath = rootPath;
    FileAdapter fileAdapter;//文件适配器

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose_path);

        listView= (ListView) findViewById(R.id.list_item);

        refreshFileList();//刷新文件列表

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickInNormalMode(position);
            }
        });
    }


    private void clickInNormalMode(int i) {
        File file = fileList.get(i);
        if (file.exists()){//如果文件存在
            currentPath = currentPath + File.separator + file.getName();//生成新的路径，相对于进入该文件夹
            refreshFileList();//刷新文件列表
        }else {//如果文件不存在
            Toast.makeText(this, "文件夹或文件不存在或已被删除", Toast.LENGTH_SHORT).show();
            refreshFileList();
        }
    }

    private void refreshFileList() {
        FileUtil fileUtil = new FileUtil(currentPath);
        fileList = fileUtil.getFileList(1);

        sortFileList(fileList);

        fileAdapter = new FileAdapter(this, fileList);
        listView.setAdapter(fileAdapter);

        // 数据改变之后刷新
        // notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容,
        // 可以实现动态的刷新列表的功能
        fileAdapter.notifyDataSetChanged();
    }

    private void sortFileList(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                if (file1.isDirectory() && file2.isDirectory() || file1.isFile() && file2.isFile()){
                    return file1.compareTo(file2);
                }else {
                    //文件夹显示在文件之前；
                    return file1.isDirectory() ? -1 : 1;
                }
            }
        });
    }

    public void cancelChoice(View view){
        finish();
    }

    public void confirmChoice(View view){
        Intent intent = new Intent();
        intent.putExtra("path", currentPath);
        setResult(RESULT_OK, intent);
        finish();
    }
}
