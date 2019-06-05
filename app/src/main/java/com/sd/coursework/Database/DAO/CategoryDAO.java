package com.sd.coursework.Database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.sd.coursework.Database.Entity.Category;

import java.util.List;

@Dao
public interface CategoryDAO {

    @Insert
    void insert(Category... categories);

    @Update
    void update(Category... categories);

    @Delete
    void delete(Category... categories);


    @Query("SELECT * FROM category ORDER BY list_pos ASC")
    LiveData<List<Category>> getAll();

    @Query("SELECT * FROM category WHERE id=:categoryId")
    Category getById(int categoryId);

    @Query("UPDATE category SET word_count=word_count+:changeVal WHERE id=:id")
    void changeWordCount(int id, int changeVal);

    @Query("UPDATE category SET list_pos=:newPos WHERE id=:id")
    void updateListPos(int id, int newPos);

    @Query("DELETE FROM category WHERE id in (:ids)")
    void deleteById(List<Integer> ids);

    @Query("DELETE FROM category")
    void deleteAll();

}
