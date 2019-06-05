package com.sd.coursework.Database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.sd.coursework.Database.DAO.CategoryDAO;
import com.sd.coursework.Database.DAO.ResultDAO;
import com.sd.coursework.Database.DAO.WordDAO;
import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.Entity.Word;

import static com.sd.coursework.Database.localDatabase.DATABASE_VERSION;

@Database(entities = {Category.class, Word.class, Result.class},
            version = DATABASE_VERSION,
            exportSchema = false)
public abstract class localDatabase extends RoomDatabase {
    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="localDatabase";

    public abstract CategoryDAO categoryDAO();
    public abstract WordDAO wordDAO();
    public abstract ResultDAO resultDAO();

    public static localDatabase mInstance;

    public static synchronized localDatabase getmInstance(Context context) {
        if (mInstance==null) {
            mInstance = Room.databaseBuilder(context.getApplicationContext(), localDatabase.class,DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .addCallback(callback)
                        .build();
        }
        return mInstance;
    }

    private static RoomDatabase.Callback callback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
//            new PopulateDbASyncTask(mInstance).execute();
        }
    };

    private static class PopulateDbASyncTask extends AsyncTask<Void, Void, Void> {
        private WordDAO wordDAO;
        private CategoryDAO categoryDAO;

        public PopulateDbASyncTask(localDatabase db) {
            this.wordDAO = db.wordDAO();
            this.categoryDAO = db.categoryDAO();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            categoryDAO.insert(new Category(0, "Show off","words that will make me sound intelligent",
                    "#DAF7A6",0));

            wordDAO.insert(new Word(1,0,
                    "Deya",
                    "The smartest person on earth"));
            wordDAO.insert(new Word(1,1,
                    "Narcissistic",
                    "having or showing an excessive interest in or admiration of oneself and one's physical appearance."));

            categoryDAO.changeWordCount(1,2);
            return null;
        }
    }
}
