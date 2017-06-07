package cn.wjinlong.filemanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import java.util.List;

import cn.wjinlong.filemanager.R;
import cn.wjinlong.filemanager.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_MULTISELECT = 1;

    private static int MODE=MODE_NORMAL;//运行模式

    ListView listView;
    List<File> fileList;//文件列表
    String rootPath = Environment.getExternalStorageDirectory().getPath();//根目录
    String currentPath = rootPath;

    List<File> checkedFiles = new ArrayList<>();//选中的文件列表
    FileAdapter fileAdapter;//文件适配器
    private long firstBackTime=0;//第一次按back键的时间，为了实现双击back退出

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= (ListView) findViewById(R.id.list_item);

        refreshFileList();//刷新文件列表

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (MODE){
                    case MODE_NORMAL:
                        clickInNormalMode(i);
                        break;
                    case MODE_MULTISELECT:
                        clickInMultiSelectMode();
                        break;
                    default:
                        break;
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                enterMultiSelectMode(i);
                return true;
            }
        });
    }

    private void enterMultiSelectMode(int i) {
        View operate = findViewById(R.id.file_operate);//得到文件操作菜单
        operate.setVisibility(View.VISIBLE);//设置文件操作菜单可见
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);//设置listView为多选模式
        MODE = MODE_MULTISELECT;//修改运行模式
        //printItemsStates();
        listView.setItemChecked(i,true);//设置选择的那个item为选中
        //printItemsStates();
    }

    private void printItemsStates() {
        Log.i(TAG,"被选中的数量："+listView.getCheckedItemCount());
        for (int j = 0; j < listView.getCount(); j++){
            Log.i(TAG,"item["+j+"]:"+listView.isItemChecked(j));
        }
    }

    private void exitMultiSelectMode() {
        listView.clearChoices();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        View operate = findViewById(R.id.file_operate);
        operate.setVisibility(View.GONE);
        MODE = MODE_NORMAL;
    }

    private void clickInMultiSelectMode() {
        if (listView.getCheckedItemCount()==0){
            exitMultiSelectMode();
        }
        fileAdapter.notifyDataSetChanged();

        //设置listView的choiceMode为CHOICE_MODE_MULTIPLE时，不用自己去实现点击反选。下面就是失败的代码
        /*if (listView.getCheckedItemCount()==0){
            exitMultiSelectMode();
        }else {
            if (listView.isItemChecked(i)){
                Log.i(TAG, "i="+i+", "+String.valueOf(listView.isItemChecked(i)));
                listView.setItemChecked(i,false);
                Log.i(TAG, "i="+i+", "+String.valueOf(listView.isItemChecked(i)));
                Log.i(TAG,"取消 "+i);
            }else {
                Log.i(TAG, "i="+i+", "+String.valueOf(listView.isItemChecked(i)));
                listView.setItemChecked(i,true);
                Log.i(TAG, "i="+i+", "+String.valueOf(listView.isItemChecked(i)));
                Log.i(TAG,"选中 "+i);
            }
            fileAdapter.notifyDataSetChanged();
        }
        for (int j = 0; j < listView.getCount(); j++){
            Log.i(TAG,"itememem["+j+"]:"+listView.isItemChecked(j));
        }*/
    }

    private void clickInNormalMode(int i) {
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
        switch (MODE){
            case MODE_NORMAL:
                switch (keyCode){
                    case KeyEvent.KEYCODE_BACK:
                        if (currentPath.equals(rootPath)){
                            long secondBackTime = System.currentTimeMillis();
                            if (secondBackTime - firstBackTime > 2000) {
                                Toast.makeText(MainActivity.this, "再按一次退出程序",Toast.LENGTH_SHORT).show();
                                firstBackTime = secondBackTime;
                                return true;
                            }else {
                                return super.onKeyUp(keyCode, event);
                                //System.exit(0);
                            }
                        }else {
                            currentPath = currentPath.substring(0, currentPath.lastIndexOf(File.separator));
                            refreshFileList();
                            return true;
                        }
                    default:
                        break;
                }
                break;
            case MODE_MULTISELECT:
                exitMultiSelectMode();
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void getCheckedFiles(){

        printItemsStates();

        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
        //Log.i("getCheckedFiles", String.valueOf(checkedItemPositions));
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            //Log.i("getCheckedFiles", String.valueOf(checkedItemPositions.get(i)));
            int key = checkedItemPositions.keyAt(i);
            if (checkedItemPositions.get(key)) {
                checkedFiles.add(fileList.get(key));
            }
        }
    }


    public void fileCopy(View view){

    }

    public void fileMove(View view){

    }

    public void fileDelete(View view){
        getCheckedFiles();
        Log.i(TAG,"fileDelete");
        for (File tmp : checkedFiles) {
            tmp.delete();
            Log.i(TAG,tmp.getPath());
        }
        listView.clearChoices();
        refreshFileList();
    }

    public void fileRename(View view){

    }
    public void fileShare(View view){

    }

    public void fileMore(View view){

    }
}
