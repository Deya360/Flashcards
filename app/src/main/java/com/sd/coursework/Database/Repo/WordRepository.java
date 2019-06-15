package com.sd.coursework.Database.Repo;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.sd.coursework.Database.DAO.WordDAO;
import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.Entity.WordLite;
import com.sd.coursework.Database.Entity.WordPlus;
import com.sd.coursework.Database.localDatabase;

import java.util.List;

interface AsyncWordResponse {
    void wordFetchFinish(Word returnData);
    void wordsFetchFinish(List<WordLite> returnData);
    void queryFetchFinish(List<Word> returnData);
    void queryFetchFinishLite(List<WordLite> returnData);
    void queryInsertFinish(int insertedId);
}

public class WordRepository implements AsyncWordResponse {
    private WordDAO wordDAO;
    private LiveData<List<WordPlus>> allWords;
    private MutableLiveData<Word> word  = new MutableLiveData<>();
    private MutableLiveData<List<WordLite>> words  = new MutableLiveData<>();
    private MutableLiveData<List<Word>> allWordsInCategory = new MutableLiveData<>();
    private MutableLiveData<List<WordLite>> allWordsInCategoryLite = new MutableLiveData<>();
    private MutableLiveData<Integer> lastId  = new MutableLiveData<>();

    public WordRepository(Application application) {
        localDatabase database = localDatabase.getmInstance(application);
        this.wordDAO = database.wordDAO();
        this.allWords = wordDAO.getAll();
    }

    public LiveData<List<WordPlus>> getAll() {
        return allWords;
    }

    public void insert(Word word) {
        new InsertAsyncTask(wordDAO, this).execute(word);
    }
    private static class InsertAsyncTask extends AsyncTask<Word, Void, Integer> {
        private AsyncWordResponse delegate;
        private WordDAO wordDAO;

        InsertAsyncTask(WordDAO wordDAO, AsyncWordResponse delegate) {
            this.wordDAO = wordDAO;
            this.delegate = delegate;
        }

        @Override
        protected Integer doInBackground(Word... words) {
            return (int)wordDAO.insert(words[0])[0];
        }

        @Override
        protected void onPostExecute(Integer aInt) {
            delegate.queryInsertFinish(aInt);
        }
    }
    @Override public void queryInsertFinish(int insertedId) {
        this.lastId.setValue(insertedId);
    }
    public MutableLiveData<Integer> getLastId() {
        return lastId;
    }

    public void update(Word word) {
        new UpdateAsyncTask(wordDAO).execute(word);
    }
    private static class UpdateAsyncTask extends AsyncTask<Word, Void, Void> {
        private WordDAO wordDAO;

        UpdateAsyncTask(WordDAO wordDAO) {
            this.wordDAO = wordDAO;
        }

        @Override
        protected Void doInBackground(Word... words) {
            wordDAO.update(words[0]);
            return null;
        }
    }

    public void delete(Word word) {
        new DeleteAsyncTask(wordDAO).execute(word);
    }
    private static class DeleteAsyncTask extends AsyncTask<Word, Void, Void> {
        private WordDAO wordDAO;

        DeleteAsyncTask(WordDAO wordDAO) {
            this.wordDAO = wordDAO;
        }

        @Override
        protected Void doInBackground(Word... words) {
            wordDAO.delete(words[0]);
            return null;
        }
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(wordDAO).execute();
    }
    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private WordDAO wordDAO;

        DeleteAllAsyncTask(WordDAO wordDAO) {
            this.wordDAO = wordDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wordDAO.deleteAll();
            return null;
        }
    }

    public void getAllByCategoryId(int categoryId) {
        new SelectAllByCategoryIdAsyncTask(wordDAO,categoryId, this).execute();
    }
    private static class SelectAllByCategoryIdAsyncTask extends AsyncTask<Void, Void, List<Word>> {
        private AsyncWordResponse delegate;
        private WordDAO wordDAO;
        private int categoryId;

        SelectAllByCategoryIdAsyncTask(WordDAO wordDAO, int categoryId, AsyncWordResponse delegate) {
            this.wordDAO = wordDAO;
            this.categoryId = categoryId;
            this.delegate = delegate;
        }

        @Override
        protected List<Word> doInBackground(Void... voids) {
            return wordDAO.getAllByCategoryId(categoryId);
        }

        @Override
        protected void onPostExecute(List<Word> wordPlus) {
            delegate.queryFetchFinish(wordPlus);
        }
    }
    @Override public void queryFetchFinish(List<Word> returnData) {
        this.allWordsInCategory.setValue(returnData);
    }
    public MutableLiveData<List<Word>> getAllByCategoryId() {
        return allWordsInCategory;
    }

    public void getAllByCategoryIdLite(int categoryId) {
        new SelectAllByCategoryIdLiteAsyncTask(wordDAO,categoryId, this).execute();
    }
    private static class SelectAllByCategoryIdLiteAsyncTask extends AsyncTask<Void, Void, List<WordLite>> {
        private AsyncWordResponse delegate;
        private WordDAO wordDAO;
        private int categoryId;

        SelectAllByCategoryIdLiteAsyncTask(WordDAO wordDAO, int categoryId, AsyncWordResponse delegate) {
            this.wordDAO = wordDAO;
            this.categoryId = categoryId;
            this.delegate = delegate;
        }

        @Override
        protected List<WordLite> doInBackground(Void... voids) {
            return wordDAO.getAllByCategoryIdLite(categoryId);
        }

        @Override
        protected void onPostExecute(List<WordLite> wordLites) {
            delegate.queryFetchFinishLite(wordLites);
        }
    }
    @Override public void queryFetchFinishLite(List<WordLite> returnData) {
        this.allWordsInCategoryLite.setValue(returnData);
    }
    public MutableLiveData<List<WordLite>> getAllByCategoryIdLite() {
        return allWordsInCategoryLite;
    }

    public void getById(int id) {
        new SelectByIdAsyncTask(wordDAO,id, this).execute();
    }
    private static class SelectByIdAsyncTask extends AsyncTask<Void, Void, Word> {
        private AsyncWordResponse delegate;
        private WordDAO wordDAO;
        private int categoryId;

        SelectByIdAsyncTask(WordDAO wordDAO, int categoryId, AsyncWordResponse delegate) {
            this.wordDAO = wordDAO;
            this.categoryId = categoryId;
            this.delegate = delegate;
        }

        @Override
        protected Word doInBackground(Void... voids) {
            return wordDAO.getById(categoryId);
        }

        @Override
        protected void onPostExecute(Word word) {
            delegate.wordFetchFinish(word);
        }
    }
    @Override public void wordFetchFinish(Word returnData) {
        this.word.setValue(returnData);
    }
    public MutableLiveData<Word> getById() {
        return word;
    }

    public void getByIds(List<Integer> ids) {
        new SelectByIdsAsyncTask(wordDAO, ids, this).execute();
    }
    private static class SelectByIdsAsyncTask extends AsyncTask<Void, Void, List<WordLite>> {
        private AsyncWordResponse delegate;
        private WordDAO wordDAO;
        private List<Integer> ids;

        SelectByIdsAsyncTask(WordDAO wordDAO, List<Integer> wordIds, AsyncWordResponse delegate) {
            this.wordDAO = wordDAO;
            this.ids = wordIds;
            this.delegate = delegate;
        }

        @Override
        protected List<WordLite> doInBackground(Void... voids) {
            String ids2 = "," + TextUtils.join(",", ids) + ",";
            return wordDAO.getByIds(ids, ids2);
        }

        @Override
        protected void onPostExecute(List<WordLite> wordLite) {
            delegate.wordsFetchFinish(wordLite);
        }
    }
    @Override public void wordsFetchFinish(List<WordLite> returnData) {
        this.words.setValue(returnData);
    }
    public MutableLiveData<List<WordLite>> getByIds() {
        return words;
    }

    public void updateListPositions(SparseIntArray arr) {
        new SwapAsyncTask(wordDAO, arr).execute();
    }
    private static class SwapAsyncTask extends AsyncTask<Void, Void, Void> {
        private WordDAO wordDAO;
        private SparseIntArray arr;

        SwapAsyncTask(WordDAO wordDAO, SparseIntArray arr) {
            this.wordDAO = wordDAO;
            this.arr = arr;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i<arr.size(); i++) {
                wordDAO.updateListPos(arr.keyAt(i), arr.valueAt(i));
            }
            return null;
        }
    }

    public void deleteById(List<Integer> list) {
        new DeleteByIdAsyncTask(wordDAO, list).execute();
    }
    private static class DeleteByIdAsyncTask extends AsyncTask<Void, Void, Void> {
        private WordDAO wordDAO;
        private List<Integer> idList;

        DeleteByIdAsyncTask(WordDAO wordDAO, List<Integer> idList) {
            this.wordDAO = wordDAO;
            this.idList = idList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wordDAO.deleteById(idList);
            return null;
        }
    }
}
