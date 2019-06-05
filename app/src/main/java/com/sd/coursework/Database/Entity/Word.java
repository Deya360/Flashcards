package com.sd.coursework.Database.Entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "word",
        foreignKeys = @ForeignKey(entity = Category.class,
                                  parentColumns = "id",
                                  childColumns = "category_id",
                                  onDelete = CASCADE))
public class Word {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private int id;

    @ColumnInfo(name="category_id", index = true)
    private int categoryId;

    @ColumnInfo(name="list_pos")
    private int listPosition;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="definition")
    private String definition;

    @ColumnInfo(name="image_uuid")
    private String imageUUID = "";

    @Ignore
    private boolean checked = false;


    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public void setChecked(boolean checked) { this.checked = checked; }

    public int getId() {
        return id;
    }
    public int getCategoryId() {
        return categoryId;
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
    public boolean isChecked() { return checked; }


    public Word(int categoryId, int listPosition,
                String name, String definition, String imageUUID) {
        this(categoryId,listPosition,name,definition);
        this.imageUUID = imageUUID;
    }

    @Ignore
    public Word(int categoryId, int listPosition,
                String name, String definition) {
        this.categoryId = categoryId;
        this.listPosition = listPosition;
        this.name = name;
        this.definition = definition;
    }
}
