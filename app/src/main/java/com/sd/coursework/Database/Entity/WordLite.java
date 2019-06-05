package com.sd.coursework.Database.Entity;

import android.arch.persistence.room.ColumnInfo;

public class WordLite {

    @ColumnInfo(name="id")
    int wordId;
    @ColumnInfo(name="name")
    String wordName;
    @ColumnInfo(name="definition")
    String wordDef;

    public WordLite(int wordId, String wordName, String wordDef) {
        this.wordId = wordId;
        this.wordName = wordName;
        this.wordDef = wordDef;
    }

    public int getWordId() {
        return wordId;
    }
    public String getWordName() {
        return wordName;
    }
    public String getWordDef() {
        return wordDef;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }
    public void setWordName(String wordName) {
        this.wordName = wordName;
    }
    public void setWordDef(String wordDef) {
        this.wordDef = wordDef;
    }
}
