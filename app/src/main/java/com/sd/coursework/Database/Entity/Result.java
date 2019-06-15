package com.sd.coursework.Database.Entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.sd.coursework.Database.Entity.TypeConverters.ArrayListConverter;
import com.sd.coursework.Database.Entity.TypeConverters.TimestampConverter;

import java.util.ArrayList;
import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "Result",
        foreignKeys = @ForeignKey(entity = Category.class,
                                  parentColumns = "id",
                                  childColumns = "category_id",
                                  onDelete = CASCADE))

public class Result {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private int id;

    @ColumnInfo(name="category_id", index = true)
    private int categoryId;

    @ColumnInfo(name="datetime")
    @TypeConverters(TimestampConverter.class)
    private Date takenTS;

    @ColumnInfo(name="word_correct_count")
    private int correctWordCnt;

    @ColumnInfo(name="word_wrong_count")
    private int wrongWordCnt;

    @ColumnInfo(name="words_id_csv")
    @TypeConverters(ArrayListConverter.class)
    private ArrayList<String> wordIds;

    public Result(int categoryId, Date takenTS, int correctWordCnt, int wrongWordCnt, ArrayList<String> wordIds) {
        this.categoryId = categoryId;
        this.takenTS = takenTS;
        this.correctWordCnt = correctWordCnt;
        this.wrongWordCnt = wrongWordCnt;
        this.wordIds = wordIds;
    }

    public int getId() {
        return id;
    }
    public int getCategoryId() {
        return categoryId;
    }
    public int getCorrectWordCnt() {
        return correctWordCnt;
    }
    public int getWrongWordCnt() {
        return wrongWordCnt;
    }
    public Date getTakenTS() {
        return takenTS;
    }
    public ArrayList<String> getWordIds() {
        return wordIds;
    }
    public void setId(int id) {
        this.id = id;
    }
}

