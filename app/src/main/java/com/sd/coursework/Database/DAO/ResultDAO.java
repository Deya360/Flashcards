package com.sd.coursework.Database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.sd.coursework.Database.Entity.Result;

import java.util.List;

@Dao
public interface ResultDAO {

    @Query("SELECT * FROM result WHERE category_id=:categoryId ORDER BY datetime DESC")
    List<Result> getAllByCategoryId(int categoryId);

    @Query("SELECT * FROM result WHERE id=:id")
    Result getById(int id);

    @Insert
    long[] insert(Result... results);

    @Update
    void update(Result... results);

    @Delete
    void delete(Result... results);

    @Query("DELETE FROM result")
    void deleteAll();

}
