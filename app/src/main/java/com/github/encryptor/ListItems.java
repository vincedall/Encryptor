package com.github.encryptor;

import android.graphics.Bitmap;

public class ListItems{

    private int icon = R.drawable.file;
    private String title;
    private boolean isImage;
    private Bitmap image = null;
    private boolean checked;
    private int size;
    private boolean isFile;

    public ListItems(int icon, String title, boolean isImage, int size, boolean isFile) {
        this.icon = icon;
        this.title = title;
        this.isImage = isImage;
        this.size = size;
        this.isFile = isFile;
    }

    public ListItems(String title, boolean isImage, Bitmap image, int size, boolean isFile){
        this.title = title;
        this.isImage = isImage;
        this.image = image;
        this.size = size;
        this.isFile = isFile;
    }

    public int getIcon(){
        return icon;
    }
    public String getTitle(){
        return title;
    }
    public boolean getIsImage(){
        return isImage;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    public Bitmap getImage() {
        return image;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getSize() {
        return size;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}