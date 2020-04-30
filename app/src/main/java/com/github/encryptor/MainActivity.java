package com.github.encryptor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.crypto.spec.IvParameterSpec;

import static java.lang.Thread.sleep;

/**
 *Main GUI of the application.
 *@author Vincent Dallaire
 *@version 1.0
 *@since 2019-03-31
 */
public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ListAdapter adapter;
    private TextView directory;
    private String selectedFilePath = null;
    private byte[] salt;
    private IvParameterSpec iv;
    private String currentDirectory;
    private ArrayList<ListItems> itemsList = new ArrayList<>();
    private Button move;
    private Button copy;
    private ImageView trash;
    private File file;
    private int listViewIndex;
    private int listViewTop;
    ImagesAsyncTask imagesTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        Ask for permission to read external storage. As long as not accepted, wait.
         */
        String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permission, 200);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            try {
                while(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED)
                    sleep(1000);
            } catch (InterruptedException e) {
                finish();
            }
        }
        /*Create default salt and IV changed at encryption time*/
        try {
            salt = "0000000000000000".getBytes("UTF-8");
            iv = new IvParameterSpec("0000000000000000".getBytes("UTF-8"));
        }catch(Exception e){}
        listView = findViewById(R.id.list_view);
        directory = findViewById(R.id.directory);
        trash = findViewById(R.id.trash);
        initialize(); //Initialise listView adapter and currentDirectory as the root directory of filesystem
        setListViewWithIndex(); //Set the listview and index placement
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                increaseDirectory(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = itemsList.get(position).getTitle();
                if (filename.length() > 3) {
                    String extension = filename.substring(filename.length() - 4);
                    if (extension.equals(".apk") || extension.equals(".txt") || extension.equals(".html") ||
                            extension.equals(".htm") || extension.equals(".xml") || extension.equals(".jpg") ||
                            extension.equals(".png") || extension.equals(".mp3") || extension.equals(".aac")||
                            extension.equals(".wav") || extension.equals(".ogg") || extension.equals(".m4a") ||
                            extension.equals(".mp4") || extension.equals(".mkv") || extension.equals(".avi")||
                            extension.equals(".webm") || extension.equals(".pdf")) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if (extension.equals(".apk")) {
                                intent.setDataAndType(Uri.parse("file://" + currentDirectory + "/" + filename),
                                        "application/vnd.android.package-archive");
                            } else if (extension.equals(".txt") || extension.equals(".html") ||
                                    extension.equals(".htm") || extension.equals(".xml")) {
                                intent.setDataAndType(Uri.parse("file://" + currentDirectory + "/" + filename), "text/*");
                            } else if (extension.equals(".jpg") || extension.equals(".png")) {
                                intent.setDataAndType(Uri.parse("file://" + currentDirectory + "/" + filename), "image/*");
                            } else if (extension.equals(".mp3") || extension.equals(".aac") ||
                                    extension.equals(".wav") || extension.equals(".ogg") ||
                                    extension.equals(".m4a")) {
                                intent.setDataAndType(Uri.parse("file://" + currentDirectory + "/" + filename), "audio/*");
                            } else if (extension.equals(".mp4") || extension.equals(".mkv") ||
                                    extension.equals(".avi") || extension.equals(".webm")) {
                                intent.setDataAndType(Uri.parse("file://" + currentDirectory + "/" + filename), "video/*");
                            } else if (extension.equals(".pdf")) {
                                intent.setDataAndType(Uri.parse("file://" + currentDirectory + "/" + filename), "application/pdf");
                            }
                            startActivity(intent);
                        } else {
                            File file = new File(currentDirectory + "/" + filename);
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(),
                                    getPackageName() + ".fileprovider", file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if (extension.equals(".apk")) {
                                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                            } else if (extension.equals(".txt") || extension.equals(".html") ||
                                    extension.equals(".htm") || extension.equals(".xml")) {
                                intent.setDataAndType(uri, "text/*");
                            } else if (extension.equals(".jpg") || extension.equals(".png")) {
                                intent.setDataAndType(uri, "image/*");
                            } else if (extension.equals(".mp3") || extension.equals(".aac") ||
                                    extension.equals(".wav") || extension.equals(".ogg") ||
                                    extension.equals(".m4a")) {
                                intent.setDataAndType(uri, "audio/*");
                            } else if (extension.equals(".mp4") || extension.equals(".mkv") ||
                                    extension.equals(".avi") || extension.equals(".webm")) {
                                intent.setDataAndType(uri, "video/*");
                            } else if (extension.equals(".pdf")) {
                                intent.setDataAndType(uri, "application/pdf");
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }
                    }
                }
                return true;
            }
        });
        move = findViewById(R.id.move_here);
        copy = findViewById(R.id.copy_here);
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move.setVisibility(View.GONE);
                if((currentDirectory +"/"+ file.getName()).equals(file.getAbsolutePath())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Not permitted").setMessage("You can't move that here");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else if(new File(currentDirectory +"/"+ file.getName()).exists()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("A file with the same name exists").setMessage("Do you want to replace it");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            InputStream is = null;
                            OutputStream os = null;
                            try {
                                is = new FileInputStream(file);
                                os = new FileOutputStream(new File(currentDirectory + "/" + file.getName()));
                                byte[] b = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = is.read(b)) > 0) {
                                    os.write(b, 0, bytesRead);
                                }
                            } catch (Exception e) {

                            } finally {
                                try {
                                    if (is != null)
                                        is.close();
                                    if (os != null)
                                        os.close();
                                } catch (Exception e) {
                                }
                            }
                            setItemsList();
                            setListViewWithIndex();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = new FileInputStream(file);
                        os = new FileOutputStream(new File(currentDirectory + "/" + file.getName()));
                        byte[] b = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(b)) > 0) {
                            os.write(b, 0, bytesRead);
                        }
                    } catch (Exception e) {

                    } finally {
                        try {
                            if (is != null)
                                is.close();
                            if (os != null)
                                os.close();
                        } catch (Exception e) {
                        }
                    }
                    file.delete();
                    setItemsList();
                    setListViewWithIndex();
                }
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy.setVisibility(View.GONE);
                if(new File(currentDirectory +"/"+ file.getName()).exists()){
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = new FileInputStream(file);
                        String copyNumber = getCopyNumber(file.getAbsolutePath(), 1);
                        os = new FileOutputStream(new File(currentDirectory + "/" + file.getName() + copyNumber));
                        byte[] b = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(b)) > 0) {
                            os.write(b, 0, bytesRead);
                        }
                    } catch (Exception e) {

                    } finally {
                        try {
                            if (is != null)
                                is.close();
                            if (os != null)
                                os.close();
                        } catch (Exception e) {
                        }
                    }
                    setItemsList();
                    setListViewWithIndex();
                }else {
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = new FileInputStream(file);
                        os = new FileOutputStream(new File(currentDirectory + "/" + file.getName()));
                        byte[] b = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(b)) > 0) {
                            os.write(b, 0, bytesRead);
                        }
                    } catch (Exception e) {

                    } finally {
                        try {
                            if (is != null)
                                is.close();
                            if (os != null)
                                os.close();
                        } catch (Exception e) {
                        }
                    }
                    setItemsList();
                    setListViewWithIndex();
                }
            }
        });
        Button backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Button addFolder = findViewById(R.id.add_folder);
        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                dialog.setContentView(R.layout.add_folder);
                final Button okButton = dialog.findViewById(R.id.ok);
                final Button cancelButton = dialog.findViewById(R.id.cancel);
                final EditText folderName = dialog.findViewById(R.id.folder_name);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!folderName.getText().toString().equals(""))
                            new File(new File(currentDirectory) +"/"+ folderName.getText().toString()).mkdirs();
                        dialog.dismiss();
                        setItemsList();
                        setListViewWithIndex();
                    }
                });
                dialog.show();
            }
        });
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete selection").setMessage("Do you want to delete the selection");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (ListItems item : itemsList){
                            if (item.getChecked()){
                                if(new File(currentDirectory +"/"+ item.getTitle()).delete()) {
                                }else{
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Folder not empty").setMessage("One of the folders is not empty. It was not deleted.");
                                    AlertDialog d = builder.create();
                                    d.show();
                                }
                            }
                        }
                        disableTrash();
                        setItemsList();
                        setListViewWithIndex();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public String getCopyNumber(String path, int number){
        while (new File(path + "(" + number + ")").exists()) {
            number++;
        }
        return "(" + number + ")";
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu, menu);
    }

    public void decreaseDirectory(){
        int a = 0;
        for (int i = currentDirectory.length() - 1; i > -1; i--){
            if (currentDirectory.charAt(i) == '/'){
                a = i;
                break;
            }
        }
        currentDirectory = currentDirectory.substring(0, a);
        setItemsList();
    }

    public void setItemsList(){
        itemsList = new ArrayList<>();
        ArrayList<File> images = new ArrayList<>();
        try {
            for (File file : new File(currentDirectory).listFiles()) {
                String extension = getFileExtension(file);
                if (extension.equals(".apk") || extension.equals(".txt") || extension.equals(".html") ||
                        extension.equals(".htm") || extension.equals(".xml") || extension.equals(".jpg") ||
                        extension.equals(".png") || extension.equals(".mp3") || extension.equals(".aac")||
                        extension.equals(".wav") || extension.equals(".ogg") || extension.equals(".m4a") ||
                        extension.equals(".mp4") || extension.equals(".mkv") || extension.equals(".avi")||
                        extension.equals(".webm") || extension.equals(".pdf")){
                    switch(extension){
                        case (".apk"):
                            try {
                                PackageManager pm = getPackageManager();
                                PackageInfo pi = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
                                pi.applicationInfo.sourceDir = file.getAbsolutePath();
                                pi.applicationInfo.publicSourceDir = file.getAbsolutePath();
                                Drawable icon = pi.applicationInfo.loadIcon(pm);
                                Bitmap bt = ((BitmapDrawable) icon).getBitmap();
                                itemsList.add(new ListItems(file.getName(), true, bt, (int) file.length(), true));
                            }catch(Exception e) {
                                itemsList.add(new ListItems(R.drawable.file, file.getName(), false, (int) file.length(), true));
                            }
                            break;
                        case (".txt"):
                        case (".html"):
                        case (".htm"):
                        case (".xml"):
                            itemsList.add(new ListItems(R.drawable.txt, file.getName(), false, (int) file.length(), true));
                            break;
                        case (".jpg"):
                        case (".png"):
                            images.add(file);
                            break;
                        case (".mp3"):
                        case (".aac"):
                        case (".wav"):
                        case (".ogg"):
                        case (".m4a"):
                            itemsList.add(new ListItems(R.drawable.audio, file.getName(), false, (int) file.length(), true));
                            break;
                        case (".mp4"):
                        case (".mkv"):
                        case (".avi"):
                        case (".webm"):
                            itemsList.add(new ListItems(R.drawable.video, file.getName(), false, (int) file.length(), true));
                            break;
                        case (".pdf"):
                            itemsList.add(new ListItems(R.drawable.pdf, file.getName(), false, (int) file.length(), true));
                            break;
                    }
                } else if (file.isDirectory()) {
                    itemsList.add(new ListItems(R.drawable.folder, file.getName(), false, (int) file.length(), false));
                } else if(file.isFile())
                    itemsList.add(new ListItems(R.drawable.file, file.getName(), false, (int) file.length(), true));
            }
            if (images.size() >= 10){
                for (File file : images){
                    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = Bitmap.createBitmap(10, 10, conf);
                    itemsList.add(new ListItems(file.getName(), true, bitmap, (int) file.length(), true));
                }
                imagesTask = new ImagesAsyncTask(images, itemsList, this);
                imagesTask.execute();
            }else{
                for (File file : images){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 64;
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                    itemsList.add(new ListItems(file.getName(), true, bitmap, (int) file.length(), true));
                }
            }
        }catch(NullPointerException e){
            Log.e("External files error", "not accessible");
        }
    }

    private String getFileExtension(File file){
        String name = file.getName();
        if (name.length() > 3)
            return name.substring(name.length() - 4);
        else
            return "no_extension";
    }

    public void enableTrash(){
        int counter = 0;
        for (ListItems item : itemsList){
            if (item.getChecked()){
                counter++;
            }
        }
        if (counter > 0){
            move.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
            trash.setVisibility(View.VISIBLE);
        }else{
            trash.setVisibility(View.GONE);
        }
    }

    public void disableTrash(){
        for (ListItems item : itemsList){
            item.setChecked(false);
        }
        trash.setVisibility(View.GONE);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.encrypt:
                initiateEncryptMenu();
                break;
            case R.id.delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure?").setMessage("It cannot be undone.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new File(selectedFilePath).delete();
                        setItemsList();
                        setListViewWithIndex();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.decrypt:
                initiateDecryptMenu();
                break;
            case R.id.move:
                file = new File(selectedFilePath);
                move.setVisibility(View.VISIBLE);
                copy.setVisibility(View.GONE);
                trash.setVisibility(View.GONE);
                break;
            case R.id.copy:
                file = new File(selectedFilePath);
                copy.setVisibility(View.VISIBLE);
                move.setVisibility(View.GONE);
                trash.setVisibility(View.GONE);
                break;
            case R.id.rename:
                rename();
                break;
        }
        return true;
    }

    public void rename(){
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.rename);
        final EditText filename = dialog.findViewById(R.id.file_name);
        final Button okButton = dialog.findViewById(R.id.ok_rename);
        final Button cancelButton = dialog.findViewById(R.id.cancel_rename);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filename.getText().toString().equals(""))
                    new File(selectedFilePath).renameTo(new File(currentDirectory +"/"+ filename.getText().toString()));
                dialog.dismiss();
                setItemsList();
                setListViewWithIndex();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public int encrypt(String password, String filePath, Dialog dialog){
        dialog.dismiss();
        EncryptAsyncTask encryptAsyncTask = new EncryptAsyncTask(filePath, password,this, iv, salt);
        encryptAsyncTask.execute();
        return 0;
    }

    public int decrypt(String password, String filePath, Dialog dialog){
        dialog.dismiss();
        DecryptAsyncTask decryptAsyncTask = new DecryptAsyncTask(filePath, password,this, iv, salt);
        decryptAsyncTask.execute();
        return 0;
    }

    public void initiateEncryptMenu(){
        if (imagesTask != null)
            imagesTask.cancel(true);
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.encrypt_menu);
        final Button okButton = dialog.findViewById(R.id.ok_encrypt_button);
        final Button cancelButton = dialog.findViewById(R.id.cancel_encrypt_button);
        final EditText password = dialog.findViewById(R.id.encrypt_password_edit);
        final EditText passwordConfirm = dialog.findViewById(R.id.password_confirm);
        okButton.setEnabled(false);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                okButton.setEnabled(s.toString().length() != 0);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().equals(passwordConfirm.getText().toString()) &
                        !new File(selectedFilePath + ".enc").exists()) {
                    int code = encrypt(password.getText().toString(), selectedFilePath, dialog);
                    if (code == 0) {
                        setItemsList();
                        adapter = new CustomAdapter(MainActivity.this, itemsList);
                        listView.setAdapter(adapter);
                    }else if(code == 1){
                        TextView tv = dialog.findViewById(R.id.error);
                        tv.setText("IOException: Try again");
                    }else
                        finish();
                }else{
                    if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
                        TextView tv = dialog.findViewById(R.id.error);
                        tv.setText("Passwords don't match");
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Are you sure?").setMessage(selectedFilePath + ".enc already exists. It will be overwritten");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                int code = encrypt(password.getText().toString(), selectedFilePath, dialog);
                                if (code == 0) {
                                    setItemsList();
                                    setListViewWithIndex();
                                } else
                                    finish();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        AlertDialog d = builder.create();
                        d.show();
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void initiateDecryptMenu(){
        if (imagesTask != null)
            imagesTask.cancel(true);
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setContentView(R.layout.decrypt_menu);
        final Button okButton = dialog.findViewById(R.id.ok_decrypt_button);
        final Button cancelButton = dialog.findViewById(R.id.cancel_decrypt_button);
        final EditText password = dialog.findViewById(R.id.decrypt_password_edit);
        okButton.setEnabled(false);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                okButton.setEnabled(s.toString().length() != 0);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int code = decrypt(password.getText().toString(), selectedFilePath, dialog);
                if(code == 0){
                    dialog.dismiss();
                    setItemsList();
                    setListViewWithIndex();
                }else{
                    TextView tv = dialog.findViewById(R.id.decrypt_error);
                    tv.setText("Exception thrown: try again");
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void increaseDirectory(int position){
        if (new File(currentDirectory +"/"+ itemsList.get(position).getTitle()).isDirectory()){
            currentDirectory = currentDirectory +"/"+ itemsList.get(position).getTitle();
            setItemsList();
            directory.setText(currentDirectory);
            setListViewWithIndex();
            disableTrash();
        }else{
            move.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
            selectedFilePath = currentDirectory +"/"+ itemsList.get(position).getTitle();
            openContextMenu(listView);
        }
    }

    public void initialize(){
        currentDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        setItemsList();
        directory.setText(currentDirectory);
    }

    @Override
    public void onBackPressed(){
        if(imagesTask != null)
            imagesTask.cancel(true);
        if(currentDirectory.equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
            finish();
        else {
            decreaseDirectory();
            directory.setText(currentDirectory);
            setListView();
        }
    }

    public void setListViewWithIndex(){
        if (imagesTask != null)
            if (!imagesTask.getStatus().equals(AsyncTask.Status.RUNNING))
                disableTrash();
        listViewIndex = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        listViewTop = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
        adapter = new CustomAdapter(MainActivity.this, itemsList);
        listView.setAdapter(adapter);
        listView.setSelectionFromTop(listViewIndex, listViewTop);
    }

    public void setListView(){
        if (imagesTask != null)
            if (!imagesTask.getStatus().equals(AsyncTask.Status.RUNNING))
                disableTrash();
        adapter = new CustomAdapter(MainActivity.this, itemsList);
        listView.setAdapter(adapter);
        listView.setSelectionFromTop(listViewIndex, listViewTop);
    }
}
