package cn.wjinlong.filemanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.wjinlong.filemanager.R;
import cn.wjinlong.filemanager.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    List<File> fileList;
    //String currentPath = Environment.getExternalStorageDirectory().getPath();
    String rootPath = Environment.getExternalStorageDirectory().getPath();
    String currentPath = rootPath;
    ArrayList<HashMap<String,String>> list = new ArrayList<>();

    FileAdapter fileAdapter;
    private long firstBackTime=0;

    boolean isChooseMod = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= (ListView) findViewById(R.id.list_item);

        refreshFileList();//刷新文件列表

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isChooseMod){
                    File file = fileList.get(i);

                    if (file.exists()){//如果文件存在
                        if (file.isDirectory()) {//如果是目录文件
                            currentPath = currentPath + File.separator + file.getName();//生成新的路径，相对于进入该文件夹
                            refreshFileList();//刷新文件列表
                        } else {//如果是普通文件
                            //Toast.makeText(MainActivity.this, "打开"+file.getName(), Toast.LENGTH_SHORT).show();
                            Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            FileUtil fileUtil = new FileUtil(currentPath);
                            intent.setDataAndType(path, fileUtil.getMIMEType());

                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }else {//如果文件不存在
                        Toast.makeText(MainActivity.this, "文件夹或文件不存在或已被删除", Toast.LENGTH_SHORT).show();
                        refreshFileList();
                    }
                }else {
                    if (listView.isItemChecked(i)){
                        listView.setItemChecked(i,false);
                    }else {
                        listView.setItemChecked(i,true);
                    }
                    fileAdapter.notifyDataSetChanged();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                isChooseMod = true;
                listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                View operate = findViewById(R.id.file_operate);
                operate.setVisibility(View.VISIBLE);
                listView.setItemChecked(i,true);
                return true;
            }
        });
    }


    private void refreshFileList() {
        FileUtil fileUtil = new FileUtil(currentPath);
        fileList = fileUtil.getFileList();

        sortFileList(fileList);

        fileAdapter = new FileAdapter(this, fileList);
        listView.setAdapter(fileAdapter);

        // 数据改变之后刷新
        // notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容,
        // 可以实现动态的刷新列表的功能
        fileAdapter.notifyDataSetChanged();
    }

    /**
     * 对文件列表进行排序（以文件名）
     * @param fileList
     */
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

    /**
     * 双击退出
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if (currentPath.equals(rootPath)){
                    long secondBackTime = System.currentTimeMillis();
                    if (secondBackTime - firstBackTime > 2000) {
                        Toast.makeText(MainActivity.this, "再按一次退出程序",Toast.LENGTH_SHORT).show();
                        firstBackTime = secondBackTime;
                        return true;
                    }else {
                        System.exit(0);
                    }
                }else {
                    currentPath = currentPath.substring(0, currentPath.lastIndexOf(File.separator));
                    refreshFileList();
                    return true;
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void fileCopy(View view){

    }
}
