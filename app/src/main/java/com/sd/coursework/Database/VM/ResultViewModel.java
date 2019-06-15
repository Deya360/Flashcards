package com.sd.coursework.Database.VM;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.Repo.ResultRepository;

import java.util.List;

public class ResultViewModel extends AndroidViewModel {
    private ResultRepository repository;
    private MutableLiveData<Result> result;
    private MutableLiveData<List<Result>> results = new MutableLiveData<>();
    private MutableLiveData<List<Result>> allResultsForCategory;
    private MutableLiveData<Integer> lastId;

    public ResultViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ResultRepository(application);
        this.result = repository.getById();
        this.results = repository.getPairById();
        this.allResultsForCategory = repository.getAllByCategoryId();
        this.lastId = repository.getLastId();
    }

    public void insert(Result result) {
        repository.insert(result);
    }
    public void update(Result result) {
        repository.update(result);
    }
    public void delete(Result result) {
        repository.delete(result);
    }
    public void deleteAll() {
        repository.deleteAll();
    }


    public void getAllByCategoryId(int categoryId) {
        repository.getAllByCategoryId(categoryId);
    }
    public MutableLiveData<List<Result>> getAllByCategoryId() {
        return allResultsForCategory;
    }

    public void getResult(int id) {
        repository.getById(id);
    }
    public MutableLiveData<Result> getResult() {
        return result;
    }

    public void getPairResult(int id) {
        repository.getPairById(id);
    }
    public MutableLiveData<List<Result>> getPairResult() {
        return results;
    }

    public MutableLiveData<Integer> getLastId() {
        return lastId;
    }
}
