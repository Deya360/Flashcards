package com.sd.coursework;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/* is a housing activity, it's function is to host the WordDetail fragment*/
public class WordDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        String wordName = "";
        int categoryId;
        if (getIntent().getExtras() != null){
            wordName = getIntent().getExtras().getString("wordName");
            categoryId = getIntent().getExtras().getInt("categoryId");

            startNewWordFrag(wordName, categoryId);
        }
    }

    private void startNewWordFrag(String wordName, int categoryId) {
        String tag = getResources().getString(R.string.fragment_word_detail_tag);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment == null) fragment = new WordDetailFragment();

        /* pass the position in a bundle */
        Bundle bundle = new Bundle();
        bundle.putInt("wordId", -1); // -1 means: open fragment in word addition mode
        bundle.putString("wordName", wordName);
        bundle.putInt("categoryId", categoryId);
        fragment.setArguments(bundle);

        manager.beginTransaction()
                .replace(R.id.word_contentLy, fragment, tag)
                .commit();
    }
}
