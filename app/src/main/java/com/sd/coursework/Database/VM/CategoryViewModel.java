package com.sd.coursework.Database.VM;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.Repo.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {
    private CategoryRepository repository;
    private LiveData<List<Category>> allCategories;
    private MutableLiveData<Category> category;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CategoryRepository(application);
        this.allCategories = repository.getAll();
        this.category = repository.getById();
    }

    public void insert(Category category) {
        repository.insert(category);
    }

    public void update(Category category) {
        repository.update(category);
    }

    public void delete(Category category) {
        repository.delete(category);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void updateListPositions(SparseIntArray arr) {repository.updateListPositions(arr);}

    public void deleteDiscrete(List<Integer> arr) {repository.deleteById(arr);}

    public void updateWordCount(int id, int changeVal) {repository.updateWordCount(id,changeVal);}

    public void getById(int id) {
        repository.getById(id);
    }
    public MutableLiveData<Category> getById() {
        return category;
    }
}
