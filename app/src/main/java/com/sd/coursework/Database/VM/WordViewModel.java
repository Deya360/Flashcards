package com.sd.coursework.Database.VM;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.Entity.WordLite;
import com.sd.coursework.Database.Entity.WordPlus;
import com.sd.coursework.Database.Repo.WordRepository;

import java.util.List;

public class WordViewModel extends AndroidViewModel {
    private WordRepository repository;
    private LiveData<List<WordPlus>> allWords;
    private MutableLiveData<Word> word;
    private MutableLiveData<List<Word>> allWordsInCategory;
    private MutableLiveData<List<WordLite>> allWordsInCategoryLite;
    private MutableLiveData<Integer> lastId;

    public WordViewModel(@NonNull Application application) {
        super(application);
        this.repository = new WordRepository(application);
        this.allWords = repository.getAll();
        this.word =repository.getById();
        this.allWordsInCategory = repository.getAllByCategoryId();
        this.allWordsInCategoryLite = repository.getAllByCategoryIdLite();
        this.lastId = repository.getLastId();
    }

    public void insert(Word word) {
        repository.insert(word);
    }

    public void update(Word word) {
        repository.update(word);
    }

    public void delete(Word word) {
        repository.delete(word);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public LiveData<List<WordPlus>> getAllWords() {
        return allWords;
    }

    public void getAllByCategoryId(int categoryId) {
        repository.getAllByCategoryId(categoryId);
    }
    public MutableLiveData<List<Word>> getAllByCategoryId() {
        return allWordsInCategory;
    }

    public void getAllByCategoryIdLite(int categoryId) {
        repository.getAllByCategoryIdLite(categoryId);
    }
    public MutableLiveData<List<WordLite>> getAllByCategoryIdLite() {
        return allWordsInCategoryLite;
    }


    public void updateListPositions(SparseIntArray arr) {repository.updateListPositions(arr);}

    public void deleteDiscrete(List<Integer> arr) {repository.deleteById(arr);}

    public MutableLiveData<Integer> getLastId() {
        return lastId;
    }

    public MutableLiveData<Word> getWord() {
        return word;
    }

    public void getWord(int id) {
        repository.getById(id);
    }
}
