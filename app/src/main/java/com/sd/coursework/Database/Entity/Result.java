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

    @ColumnInfo(name="word_correct_count")  //Should remove this, as it's obsolete
    private int correctWordCnt;

    @ColumnInfo(name="word_correct_id_csv")
    @TypeConverters(ArrayListConverter.class)
    private ArrayList<Integer> correctWordIds;

    @ColumnInfo(name="word_wrong_id_csv")
    @TypeConverters(ArrayListConverter.class)
    private ArrayList<Integer> wrongWordIds;

    public Result(int categoryId, Date takenTS,
                  ArrayList<Integer> correctWordIds,
                  ArrayList<Integer> wrongWordIds) {
        this.categoryId = categoryId;
        this.takenTS = takenTS;
        this.correctWordCnt = correctWordIds.size();
        this.correctWordIds =correctWordIds;
        this.wrongWordIds = wrongWordIds;
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
    public Date getTakenTS() {
        return takenTS;
    }
    public ArrayList<Integer> getCorrectWordIds() {
        return correctWordIds;
    }
    public ArrayList<Integer> getWrongWordIds() {
        return wrongWordIds;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setCorrectWordCnt(int correctWordCnt) {
        this.correctWordCnt = correctWordCnt;
    }
}

