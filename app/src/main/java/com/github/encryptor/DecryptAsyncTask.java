package com.github.encryptor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    private String filePath;
    private String password;
    private WeakReference<MainActivity> weakActivityReference;
    private IvParameterSpec iv;
    private byte[] salt;
    private Dialog progress;
    private WeakReference<ProgressBar> pb;
    private FileOutputStream fos = null;
    private FileInputStream fis = null;

    public DecryptAsyncTask(String filePath, String password, MainActivity activity, IvParameterSpec iv, byte[] salt) {
        this.filePath = filePath;
        this.password = password;
        this.weakActivityReference = new WeakReference<>(activity);
        this.iv = iv;
        this.salt = salt;
    }

    @Override
    public void onCancelled(){
    }

    @Override
    protected void onPreExecute() {   //Execution before launching background task. Called in UI thread
        super.onPreExecute();
        progress = new Dialog(weakActivityReference.get(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        progress.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progress.setContentView(R.layout.progress_bar);
        pb = new WeakReference<>((ProgressBar) progress.findViewById(R.id.progress_bar));
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                DecryptAsyncTask.this.cancel(true);
                new File(filePath + ".dec").delete();
                try {
                    if (fos != null)
                        fos.close();
                    if (fis != null)
                        fis.close();
                } catch (IOException e) { }
                fos = null;
                fis = null;
            }
        });
        pb.get().setMax((int) new File(filePath).length());
        progress.show();
    }

    @Override
    protected Integer doInBackground(Integer... params) {    //Background task
        try {
            fis = new FileInputStream(new File(filePath));
            fos = new FileOutputStream(new File(filePath + ".dec"));
            char[] passwordChars = password.toCharArray();
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passwordChars, salt, 1024, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, key, iv);
            int chunk = (int) new File(filePath).length() / 8;
            byte[] b = new byte[chunk];
            if (chunk * 8 > 1024) {
                for (int bytesRead = fis.read(b); bytesRead > -1; bytesRead = fis.read(b)) {
                    if (this.isCancelled())
                        break;
                    fos.write(c.update(b, 0, bytesRead));
                    publishProgress(bytesRead);
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }
                }
                fos.write(c.doFinal());
            }else {
                b = new byte[1024];
                int bytesRead = fis.read(b);
                fos.write(c.doFinal(b, 0, bytesRead));
                publishProgress(bytesRead);
                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new File(filePath + ".dec").delete();
            return 1;
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                Log.e("File not closed", "opened files were not closed properly");
            }
            fos = null;
            fis = null;
        }
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... length) {    //Called when using publishProgress(); method
        super.onProgressUpdate(length);                     //in doInBackground() runs on UI thread
        pb.get().setProgress(pb.get().getProgress() + length[0]);
    }

    @Override
    protected void onPostExecute(Integer integer) {     //Called in UI thread after execution
        progress.dismiss();
        if(integer == 0) {
            MainActivity activity = weakActivityReference.get();
            activity.setItemsList();
            activity.setListViewWithIndex();
        }
        if(integer == 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(weakActivityReference.get());
            builder.setTitle("Wrong password").setMessage("There was an error during decryption. You probably input the wrong password.");
            builder.show();
        }
    }
}
