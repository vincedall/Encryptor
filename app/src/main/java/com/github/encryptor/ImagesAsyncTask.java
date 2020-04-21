package com.github.encryptor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImagesAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    private ArrayList<File> images;
    private ArrayList<ListItems> itemsList;
    private WeakReference<MainActivity> weakActivityReference;

    public ImagesAsyncTask(ArrayList<File> images, ArrayList<ListItems> itemsList, MainActivity activity){
        this.images = images;
        this.itemsList = itemsList;
        this.weakActivityReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute(){   //Execution before launching background task. Called in UI thread
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {    //Background task
        int counter = 0;
        for (File file : images){
            if (this.isCancelled())
                break;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            for (ListItems item : itemsList){
                if (item.getTitle().equals(file.getName())){
                    item.setImage(bitmap);
                }
            }
            counter++;
            if (counter == 5){
                counter = 0;
                publishProgress(0);
            }
        }
        publishProgress(0);
        return 0;
    }
    @Override
    protected void onProgressUpdate(Integer... length) {    //Called when using publishProgress(); method
        super.onProgressUpdate(length);                     //in doInBackground() runs on UI thread
        MainActivity activity = weakActivityReference.get();
        activity.setListViewWithIndex();
    }

    @Override
    protected void onPostExecute(Integer integer){     //Called in UI thread after execution

    }
}
