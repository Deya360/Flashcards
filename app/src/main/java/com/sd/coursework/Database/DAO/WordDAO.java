package com.sd.coursework.Database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.sd.coursework.Database.Entity.ResultLite;
import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.Entity.WordLite;
import com.sd.coursework.Database.Entity.WordPlus;

import java.util.List;

@Dao
public interface WordDAO {

    @Insert
    long[] insert(Word... words);

    @Update
    void update(Word... words);

    @Delete
    void delete(Word... words);

    @Query("SELECT * FROM word WHERE id=:wordId")
    Word getById(int wordId);

    @Query("SELECT id, name, definition FROM word WHERE id in (:ids) ORDER BY instr(:ids2, ',' || id || ',')")
    List<WordLite> getByIds(List<Integer> ids, String ids2);

    @Query("SELECT w.id, c.id categoryId, c.name categoryName, w.list_pos, w.name, w.definition, w.image_uuid " +
            "FROM word w LEFT JOIN category c ON w.category_id=c.id ORDER BY w.name COLLATE NOCASE ASC")
    LiveData<List<WordPlus>> getAll();

    @Query("SELECT * FROM word WHERE category_id=:categoryId ORDER BY list_pos")
    List<Word> getAllByCategoryId(int categoryId);

    @Query("SELECT id, name, definition FROM word WHERE category_id=:categoryId ORDER BY list_pos")
    List<WordLite> getAllByCategoryIdLite(int categoryId);

    @Query("UPDATE word SET list_pos=:newPos WHERE id=:id")
    void updateListPos(int id, int newPos);


    @Query("DELETE FROM word WHERE id in (:ids)")
    void deleteById(List<Integer> ids);

    @Query("DELETE FROM word")
    void deleteAll();

}
