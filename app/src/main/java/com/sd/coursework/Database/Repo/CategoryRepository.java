package com.sd.coursework.Database.Repo;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.SparseIntArray;

import com.sd.coursework.Database.DAO.CategoryDAO;
import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.localDatabase;

import java.util.List;

interface AsyncCategoryResponse {
    void queryFetchFinish(Category returnData);
}

public class CategoryRepository implements AsyncCategoryResponse {
    private CategoryDAO categoryDAO;
    private LiveData<List<Category>> allCategories;
    private MutableLiveData<Category> category = new MutableLiveData<>();

    public CategoryRepository(Application application) {
        localDatabase database = localDatabase.getmInstance(application);
        this.categoryDAO = database.categoryDAO();
        this.allCategories = categoryDAO.getAll();
    }

    public void insert(Category category) {
        new InsertAsyncTask(categoryDAO).execute(category);
    }

    public void update(Category category) {
        new UpdateAsyncTask(categoryDAO).execute(category);
    }

    public void delete(Category category) {
        new DeleteAsyncTask(categoryDAO).execute(category);
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(categoryDAO).execute();
    }

    public LiveData<List<Category>> getAll() {
        return allCategories;
    }

    public void updateListPositions(SparseIntArray arr) {
        new SwapAsyncTask(categoryDAO, arr).execute();
    }

    public void deleteById(List<Integer> list) {
        new DeleteByIdAsyncTask(categoryDAO, list).execute();
    }

    public void updateWordCount(int id, int changeVal) {
        new UpdateWordCountAsyncTask(categoryDAO,id,changeVal).execute();
    }

    public MutableLiveData<Category> getById() {
        return category;
    }

    public void getById(int id) {
        new SelectByIdAsyncTask(categoryDAO, id, this).execute();
    }

    @Override
    public void queryFetchFinish(Category returnData) {
        this.category.setValue(returnData);
    }

    //AsyncTasks
    private static class InsertAsyncTask extends AsyncTask<Category, Void, Void> {
        private CategoryDAO categoryDAO;

        InsertAsyncTask(CategoryDAO categoryDAO) {
            this.categoryDAO = categoryDAO;
        }

        @Override
        protected Void doInBackground(Category... categories) {
            categoryDAO.insert(categories[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Category, Void, Void> {
        private CategoryDAO categoryDAO;

        UpdateAsyncTask(CategoryDAO categoryDAO) {
            this.categoryDAO = categoryDAO;
        }

        @Override
        protected Void doInBackground(Category... categories) {
            categoryDAO.update(categories[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Category, Void, Void> {
        private CategoryDAO categoryDAO;

        DeleteAsyncTask(CategoryDAO categoryDAO) {
            this.categoryDAO = categoryDAO;
        }

        @Override
        protected Void doInBackground(Category... categories) {
            categoryDAO.delete(categories[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private CategoryDAO categoryDAO;

        DeleteAllAsyncTask(CategoryDAO categoryDAO) {
            this.categoryDAO = categoryDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            categoryDAO.deleteAll();
            return null;
        }
    }

    private static class SwapAsyncTask extends AsyncTask<Void, Void, Void> {
        private CategoryDAO categoryDAO;
        private SparseIntArray arr;

        SwapAsyncTask(CategoryDAO categoryDAO, SparseIntArray arr) {
            this.categoryDAO = categoryDAO;
            this.arr = arr;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i<arr.size(); i++) {
                categoryDAO.updateListPos(arr.keyAt(i), arr.valueAt(i));
            }
            return null;
        }
    }

    private static class DeleteByIdAsyncTask extends AsyncTask<Void, Void, Void> {
        private CategoryDAO categoryDAO;
        private List<Integer> idList;

        DeleteByIdAsyncTask(CategoryDAO categoryDAO, List<Integer> idList) {
            this.categoryDAO = categoryDAO;
            this.idList = idList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            categoryDAO.deleteById(idList);
            return null;
        }
    }

    private static class UpdateWordCountAsyncTask extends AsyncTask<Void, Void, Void> {
        private CategoryDAO categoryDAO;
        private int id;
        private int changeVal;

        UpdateWordCountAsyncTask(CategoryDAO categoryDAO, int id, int changeVal) {
            this.categoryDAO = categoryDAO;
            this.id = id;
            this.changeVal = changeVal;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            categoryDAO.changeWordCount(id, changeVal);
            return null;
        }
    }

    private static class SelectByIdAsyncTask extends AsyncTask<Void, Void, Category> {
        private AsyncCategoryResponse delegate;
        private CategoryDAO categoryDAO;
        private int id;

        SelectByIdAsyncTask(CategoryDAO categoryDAO, int id, AsyncCategoryResponse delegate) {
            this.categoryDAO = categoryDAO;
            this.id = id;
            this.delegate = delegate;
        }

        @Override
        protected Category doInBackground(Void... voids) {
            return categoryDAO.getById(id);
        }

        @Override
        protected void onPostExecute(Category category) {
            delegate.queryFetchFinish(category);
        }
    }
}
