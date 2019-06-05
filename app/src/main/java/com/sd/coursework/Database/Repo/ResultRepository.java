package com.sd.coursework.Database.Repo;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import com.sd.coursework.Database.DAO.ResultDAO;
import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.localDatabase;

import java.util.List;

interface AsyncResultResponse {
    void resultFetchFinish(Result returnData);
    void queryFetchFinish(List<Result> returnData);
    void queryInsertFinish(int insertedId);
}

public class ResultRepository implements AsyncResultResponse {
    private ResultDAO resultDAO;
    private MutableLiveData<Result> result = new MutableLiveData<>();;
    private MutableLiveData<List<Result>> allResultsForCategory  = new MutableLiveData<>();
    private MutableLiveData<Integer> lastId  = new MutableLiveData<>();

    public ResultRepository(Application application) {
        localDatabase database = localDatabase.getmInstance(application);
        this.resultDAO = database.resultDAO();
    }


    public void insert(Result result) {
        new InsertAsyncTask(resultDAO,this).execute(result);
    }
    private static class InsertAsyncTask extends AsyncTask<Result, Void, Integer> {
        private AsyncResultResponse delegate;
        private ResultDAO resultDAO;

        InsertAsyncTask(ResultDAO resultDAO, AsyncResultResponse delegate) {
            this.resultDAO = resultDAO;
            this.delegate = delegate;
        }

        @Override
        protected Integer doInBackground(Result... results) {
            return (int)resultDAO.insert(results[0])[0];
        }

        @Override
        protected void onPostExecute(Integer insertedId) {
            delegate.queryInsertFinish(insertedId);
        }
    }
    @Override public void queryInsertFinish(int insertedId) {
        this.lastId.setValue(insertedId);
    }
    public MutableLiveData<Integer> getLastId() {
        return lastId;
    }

    public void update(Result result) {
        new UpdateAsyncTask(resultDAO).execute(result);
    }
    private static class UpdateAsyncTask extends AsyncTask<Result, Void, Void> {
        private ResultDAO resultDAO;

        UpdateAsyncTask(ResultDAO resultDAO) {
            this.resultDAO = resultDAO;
        }

        @Override
        protected Void doInBackground(Result... results) {
            resultDAO.update(results[0]);
            return null;
        }
    }

    public void delete(Result result) {
        new DeleteAsyncTask(resultDAO).execute(result);
    }
    private static class DeleteAsyncTask extends AsyncTask<Result, Void, Void> {
        private ResultDAO resultDAO;

        DeleteAsyncTask(ResultDAO resultDAO) {
            this.resultDAO = resultDAO;
        }

        @Override
        protected Void doInBackground(Result... results) {
            resultDAO.delete(results[0]);
            return null;
        }
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(resultDAO).execute();
    }
    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private ResultDAO resultDAO;

        DeleteAllAsyncTask(ResultDAO resultDAO) {
            this.resultDAO = resultDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            resultDAO.deleteAll();
            return null;
        }
    }

    public void getAllByCategoryId(int categoryId) {
        new SelectAllByCategoryIdAsyncTask(resultDAO,categoryId, this).execute();
    }
    private static class SelectAllByCategoryIdAsyncTask extends AsyncTask<Void, Void, List<Result>> {
        private AsyncResultResponse delegate;
        private ResultDAO resultDAO;
        private int categoryId;

        SelectAllByCategoryIdAsyncTask(ResultDAO resultDAO, int categoryId, AsyncResultResponse delegate) {
            this.resultDAO = resultDAO;
            this.categoryId = categoryId;
            this.delegate = delegate;
        }

        @Override
        protected List<Result> doInBackground(Void... voids) {
            return resultDAO.getAllByCategoryId(categoryId);
        }

        @Override
        protected void onPostExecute(List<Result> results) {
            delegate.queryFetchFinish(results);
        }
    }
    @Override public void queryFetchFinish(List<Result> returnData) {
        this.allResultsForCategory.setValue(returnData);
    }
    public MutableLiveData<List<Result>> getAllByCategoryId() {
        return allResultsForCategory;
    }

    public void getById(int id) {
        new SelectByIdAsyncTask(resultDAO,id, this).execute();
    }
    private static class SelectByIdAsyncTask extends AsyncTask<Void, Void, Result> {
        private AsyncResultResponse delegate;
        private ResultDAO resultDAO;
        private int categoryId;

        SelectByIdAsyncTask(ResultDAO resultDAO, int categoryId, AsyncResultResponse delegate) {
            this.resultDAO = resultDAO;
            this.categoryId = categoryId;
            this.delegate = delegate;
        }

        @Override
        protected Result doInBackground(Void... voids) {
            return resultDAO.getById(categoryId);
        }

        @Override
        protected void onPostExecute(Result result) {
            delegate.resultFetchFinish(result);
        }
    }
    @Override public void resultFetchFinish(Result returnData) {
        this.result.setValue(returnData);
    }
    public MutableLiveData<Result> getById() {
        return result;
    }

}
