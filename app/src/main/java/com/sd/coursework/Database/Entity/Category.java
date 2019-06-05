package com.sd.coursework.Database.Entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "category")
public class Category {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private int id;

    @ColumnInfo(name="list_pos")
    private int listPosition;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="description")
    private String desc;

    @ColumnInfo(name="color_hex")
    private String colorHex;

    @ColumnInfo(name="word_count")
    private int wordCount;

    @ColumnInfo(name="word_learned_count")
    private int learnedWordCnt;

    @Ignore
    private boolean checked = false;


    public int getId() {
        return id;
    }
    public int getListPosition() {
        return listPosition;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }
    public String getColorHex() {
        return colorHex;
    }
    public int getWordCount() {
        return wordCount;
    }
    public int getLearnedWordCnt() {
        return learnedWordCnt;
    }
    public boolean isChecked() { return checked; }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    public void setLearnedWordCnt(int learnedWordCnt) {
        this.learnedWordCnt = learnedWordCnt;
    }
    public void setChecked(boolean checked) { this.checked = checked; }

    public Category(int listPosition, String name, String desc,
                    String colorHex, int wordCount, int learnedWordCnt) {
        this.listPosition = listPosition;
        this.name = name;
        this.desc = desc;
        this.colorHex = colorHex;
        this.wordCount = wordCount;
        this.learnedWordCnt = learnedWordCnt;
    }

    @Ignore
    public Category(int listPosition, String name, String desc,
                    String colorHex, int wordCount) {
        this(listPosition, name, desc, colorHex, wordCount,0);
    }
}
