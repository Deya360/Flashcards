package com.sd.coursework.Database.Entity;

import android.arch.persistence.room.ColumnInfo;

public class WordPlus {
    private int id;
    private int categoryId;
    private String categoryName;
    @ColumnInfo(name="list_pos")
    private int listPosition;
    private String name;
    private String definition;
    @ColumnInfo(name="image_uuid")
    private String imageUUID;

    public int getId() {
        return id;
    }
    public int getCategoryId() {
        return categoryId;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public int getListPosition() {
        return listPosition;
    }
    public String getName() {
        return name;
    }
    public String getDefinition() {
        return definition;
    }
    public String getImageUUID() {
        return imageUUID;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }
}