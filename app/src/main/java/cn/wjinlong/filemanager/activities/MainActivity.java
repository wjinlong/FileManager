package cn.wjinlong.filemanager.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.wjinlong.filemanager.R;
import cn.wjinlong.filemanager.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

    public static final int NORMAL_MODE = 0;
    public static final int MULTI_SELECT_MODE = 1;
    private static int MODE = NORMAL_MODE;//运行模式

    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_TIME = 1;
    private static int SORT_BY = SORT_BY_NAME;

    public static final int ORDER = 1;
    public static final int REVERSE_ORDER = -1;
    private static int SORT_ORDER = ORDER;

    public static boolean SHOW_HIDE_FILES = false;

    ListView listView;
    List<File> fileList;//文件列表
    String rootPath = Environment.getExternalStorageDirectory().getPath();//根目录
    String currentPath = rootPath;

    //移动或复制的路径
    String toPath;

    public static final String QQfile_recv = Environment.getExternalStorageDirectory().getPath() + File.separator + "Tencent" + File.separator + "QQfile_recv";
    public static final String TIMfile_recv = Environment.getExternalStorageDirectory().getPath() + File.separator + "Tencent" + File.separator + "TIMfile_recv";

    List<File> checkedFiles = new ArrayList<>();//选中的文件列表
    FileAdapter fileAdapter;//文件适配器
    private long firstBackTime = 0;//第一次按back键的时间，为了实现双击back退出

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        File QQfile = new File(QQfile_recv);
//        File TIMfile = new File(TIMfile_recv);
//
//        if (QQfile.exists()){
//            rootPath = QQfile.getPath();
//            currentPath = QQfile.getPath();
//        }else if (TIMfile.exists()){
//            rootPath = TIMfile.getPath();
//            currentPath = TIMfile.getPath();
//        }

        listView = (ListView) findViewById(R.id.list_item);

        refreshFileList();//刷新文件列表

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (MODE) {
                    case NORMAL_MODE:
                        clickInNormalMode(i);
                        break;
                    case MULTI_SELECT_MODE:
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_name:
                SORT_BY = SORT_BY_NAME;
                SORT_ORDER = ORDER;
                refreshFileList();
                return true;
            case R.id.sort_by_time:
                SORT_BY = SORT_BY_TIME;
                SORT_ORDER = REVERSE_ORDER;
                refreshFileList();
                return true;

            case R.id.root_path:
                if (!rootPath.equals(Environment.getExternalStorageDirectory().getPath())) {
                    rootPath = Environment.getExternalStorageDirectory().getPath();
                    currentPath = rootPath;
                    SORT_BY = SORT_BY_NAME;
                    SORT_ORDER = ORDER;
                    refreshFileList();
                }
                return true;
            case R.id.qq_download:
                File QQfile = new File(QQfile_recv);
                if (QQfile.exists()){
                    rootPath = QQfile.getPath();
                    currentPath = QQfile.getPath();
                    SORT_BY = SORT_BY_TIME;
                    SORT_ORDER = REVERSE_ORDER;
                    refreshFileList();
                }else {
                    Toast.makeText(MainActivity.this, "您可能没有安装QQ", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.tim_download:
                File TIMfile = new File(TIMfile_recv);

                if (TIMfile.exists()) {
                    rootPath = TIMfile.getPath();
                    currentPath = TIMfile.getPath();
                    SORT_BY = SORT_BY_TIME;
                    SORT_ORDER = REVERSE_ORDER;
                    refreshFileList();
                } else {
                    Toast.makeText(MainActivity.this, "您可能没有安装TIM", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.show_hide_files:

                Log.d("status Before: ", String.valueOf(item.isChecked()));
                boolean beforeStatus = item.isChecked();
                if (beforeStatus) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }

                Log.d("status: After: ", String.valueOf(item.isChecked()));
                SHOW_HIDE_FILES = item.isChecked();
                Log.d("show_hide_files status", String.valueOf(SHOW_HIDE_FILES));
                refreshFileList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 进入多选模式
     *
     * @param i 进入多选模式时点击的Item的编号
     */
    private void enterMultiSelectMode(int i) {
        View operate = findViewById(R.id.file_operate);//得到文件操作菜单
        operate.setVisibility(View.VISIBLE);//设置文件操作菜单可见
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);//设置listView为多选模式
        MODE = MULTI_SELECT_MODE;//修改运行模式
        //printItemsStates();
        listView.setItemChecked(i, true);//设置选择的那个item为选中
        //printItemsStates();
    }

    /**
     * 打印所有Item的信息，调试使用
     */
    private void printItemsStates() {
        Log.i(TAG, "被选中的数量：" + listView.getCheckedItemCount());
        for (int j = 0; j < listView.getCount(); j++) {
            Log.i(TAG, "item[" + j + "]:" + listView.isItemChecked(j));
        }
    }

    /**
     * 退出多选模式
     */
    private void exitMultiSelectMode() {
        listView.clearChoices();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        View operate = findViewById(R.id.file_operate);
        operate.setVisibility(View.GONE);
        MODE = NORMAL_MODE;
    }

    /**
     * 多选模式下的点击事件
     */
    private void clickInMultiSelectMode() {
        if (listView.getCheckedItemCount() == 0) {
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

    /**
     * 正常模式下的点击事件
     *
     * @param i 点击的Item的编号
     */
    private void clickInNormalMode(int i) {
        File file = fileList.get(i);
        if (file.exists()) {//如果文件存在
            if (file.isDirectory()) {//如果是目录文件
                currentPath = currentPath + File.separator + file.getName();//生成新的路径，相对于进入该文件夹
                refreshFileList();//刷新文件列表
            } else {//如果是普通文件
                //Toast.makeText(MainActivity.this, "打开"+file.getName(), Toast.LENGTH_SHORT).show();
                try {

                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory("android.intent.category.DEFAULT");

                    FileUtil fileUtil = new FileUtil(file.getPath());
                    intent.setDataAndType(path, fileUtil.getMIMEType());
                    Log.d("MIME", "extension: " + fileUtil.getExtension());
                    Log.d("MIME", "type: " + fileUtil.getMIMEType());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Toast.makeText(MainActivity.this,"打开失败，你可能未安装可以打开该类型文件的应用",Toast.LENGTH_LONG).show();
                }
            }
        } else {//如果文件不存在
            Toast.makeText(MainActivity.this, "文件夹或文件不存在或已被删除", Toast.LENGTH_SHORT).show();
            refreshFileList();
        }
    }

    /**
     * 刷新文件列表
     */
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
     * 对文件列表进行排序
     *
     * @param fileList 文件列表
     */
    private void sortFileList(List<File> fileList) {
        switch (SORT_BY) {
            case SORT_BY_NAME:
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        if (file1.isDirectory() && file2.isDirectory() || file1.isFile() && file2.isFile()) {
                            return SORT_ORDER *file1.compareTo(file2);
                        } else {
                            //文件夹显示在文件之前；
                            return file1.isDirectory() ? -1 : 1;
                        }
                    }
                });
                break;
            case SORT_BY_TIME:
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        if (file1.isDirectory() && file2.isDirectory() || file1.isFile() && file2.isFile()) {
                            return SORT_ORDER * (file1.lastModified() > file2.lastModified() ? 1 : -1);
                        } else {
                            //文件夹显示在文件之前；
                            return file1.isDirectory() ? -1 : 1;
                        }
                    }
                });
                break;
            default:
        }
    }

    /**
     * 双击退出
     *
     * @param keyCode 按键码
     * @param event   事件
     * @return 是否处理完成
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (MODE) {
            case NORMAL_MODE:
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (currentPath.equals(rootPath)) {
                            long secondBackTime = System.currentTimeMillis();
                            if (secondBackTime - firstBackTime > 2000) {
                                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                                firstBackTime = secondBackTime;
                                return true;
                            } else {
                                return super.onKeyUp(keyCode, event);
                                //System.exit(0);
                            }
                        } else {
                            currentPath = currentPath.substring(0, currentPath.lastIndexOf(File.separator));
                            refreshFileList();
                            return true;
                        }
                    default:
                        break;
                }
                break;
            case MULTI_SELECT_MODE:
                exitMultiSelectMode();
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void refreshCheckedFiles() {

        printItemsStates();

        checkedFiles.clear();
        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
        //Log.i("refreshCheckedFiles", String.valueOf(checkedItemPositions));
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            //Log.i("refreshCheckedFiles", String.valueOf(checkedItemPositions.get(i)));
            int key = checkedItemPositions.keyAt(i);
            if (checkedItemPositions.get(key)) {
                checkedFiles.add(fileList.get(key));
            }
        }
    }


    public void fileCopy(View view) {
        Intent intent = new Intent(MainActivity.this, ChoosePathActivity.class);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    toPath = data.getStringExtra("path");
                    Log.d("返回的路径", toPath);
                    File dstDir = new File(toPath);

                    refreshCheckedFiles();
                    for (File file : checkedFiles) {
                        Log.d("复制文件", file.getName());
                        FileUtil.copy(file, dstDir);
                    }
                    currentPath = toPath;
                    exitMultiSelectMode();
                    refreshFileList();
                    Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    toPath = data.getStringExtra("path");
                    Log.d("返回的路径", toPath);
                    File dstDir = new File(toPath);

                    refreshCheckedFiles();
                    for (File file : checkedFiles) {
                        Log.d("移动文件", file.getName());
                        FileUtil.move(file, dstDir);
                    }
                    currentPath = toPath;
                    exitMultiSelectMode();
                    refreshFileList();
                    Toast.makeText(MainActivity.this, "移动成功", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    public void fileMove(View view) {
        Intent intent = new Intent(MainActivity.this, ChoosePathActivity.class);
        startActivityForResult(intent, 2);
    }

    public void fileDelete(View view) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle("删除");
        deleteDialog.setMessage("确认删除所选文件？");
        deleteDialog.setCancelable(false);
        deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refreshCheckedFiles();//刷新选中文件列表

                for (File file : checkedFiles) {
                    Log.d("删除文件", file.getName());
                    FileUtil.delete(file);
                }
//                LinkedList<File> files = new LinkedList<>();
//
//                for (File file : checkedFiles) {
//                    files.push(file);
//                }
//
//                while (!files.isEmpty()) {
//                    File file = files.pop();
//                    if (file.exists() && file.isFile()) {
//                        file.delete();
//                    }else if (file.exists() && file.isDirectory()){
//                        File[] listFiles = file.listFiles();
//                        if (listFiles.length == 0) {
//                            file.delete();
//                        } else {
//                            files.add(file);
//                            for (File tmp : listFiles) {
//                                files.push(tmp);
//                            }
//                        }
//                    }
//                }
                exitMultiSelectMode();
                refreshFileList();
                Toast.makeText(MainActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
            }
        });

        deleteDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        deleteDialog.show();
    }

    public void fileRename(View view) {
        refreshCheckedFiles();

        if (listView.getCheckedItemCount() == 1) {
            final AlertDialog.Builder renameDialog = new AlertDialog.Builder(this);
            renameDialog.setTitle("重命名");

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            final View renameView = inflater.inflate(R.layout.rename_dialog, null);
            renameDialog.setView(renameView);

            final EditText newName = (EditText) renameView.findViewById(R.id.new_name);
            final File oldFile = checkedFiles.get(0);

            newName.setText(oldFile.getName());
            newName.setSelectAllOnFocus(true);


            renameDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newPath;
                    if (oldFile.isFile()) {
                        newPath = oldFile.getParent() + File.separator + newName.getText().toString();
                    } else {
                        newPath = oldFile.getParent() + File.separator + newName.getText().toString() + File.separator;
                    }

//                    Log.d("path", "old file parent"+oldFile.getParent());
//                    Log.d("path", "new path"+newPath);

                    File newName = new File(newPath);

                    boolean renameResult = oldFile.renameTo(newName);
                    oldFile.setLastModified(System.currentTimeMillis());

                    if (renameResult) {
                        Toast.makeText(MainActivity.this, "重命名成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "重命名失败", Toast.LENGTH_SHORT).show();
                    }

                    exitMultiSelectMode();
                    refreshFileList();
                }
            });

            renameDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exitMultiSelectMode();
                }
            });

            renameDialog.show();//要在setButton之后，不然没有按钮。
        } else {
            Toast.makeText(MainActivity.this, "抱歉，目前只能重命名单个文件",Toast.LENGTH_SHORT).show();
        }
    }


    public void fileShare(View view) {
        refreshCheckedFiles();
        if (listView.getCheckedItemCount() == 1) {
            File file = checkedFiles.get(0);
            if (file.isFile()) {
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_DEFAULT);
                intent.setDataAndType(path, "*/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
                exitMultiSelectMode();
            }
        }else {
            Toast.makeText(MainActivity.this, "抱歉，目前只能分享单个文件",Toast.LENGTH_SHORT).show();
        }
    }

    public void fileMore(View view) {
        //创建弹出式菜单
        PopupMenu popupMenu = new PopupMenu(this, view);
        //填充菜单
        popupMenu.inflate(R.menu.file_more);

        popupMenu.setGravity(Gravity.TOP);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.file_details:
                        Toast.makeText(MainActivity.this, "属性", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder detailsDialog = new AlertDialog.Builder(MainActivity.this);
                        detailsDialog.setTitle("属性");

                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                        View detailsView = inflater.inflate(R.layout.details_dialog, null);
                        detailsDialog.setView(detailsView);

                        refreshCheckedFiles();
                        int totals = 0;
                        int folders = 0;
                        int files = 0;
                        String path = checkedFiles.get(0).getParent().toString();
                        long totalSize = 0;

                        for (File file : checkedFiles) {
                            if (file.isFile()) {
                                ++files;
                                totalSize += file.length();
                            } else {
                                ++folders;
                                totalSize += file.length();
                            }
                            ++totals;
                        }

                        TextView totalsView = (TextView) detailsView.findViewById(R.id.totals);
                        TextView foldersView = (TextView) detailsView.findViewById(R.id.folders);
                        TextView filesView = (TextView) detailsView.findViewById(R.id.files);
                        TextView pathView = (TextView) detailsView.findViewById(R.id.path);
                        TextView totalSizeView = (TextView) detailsView.findViewById(R.id.total_size);

                        totalsView.setText(String.valueOf(totals));
                        foldersView.setText(String.valueOf(folders));
                        filesView.setText(String.valueOf(files));
                        pathView.setText(path);

                        FileAdapter adapter = new FileAdapter();
                        totalSizeView.setText(adapter.fileSize(totalSize));

                        detailsDialog.setPositiveButton("确定", null);
                        detailsDialog.show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        //显示
        popupMenu.show();
    }
}
