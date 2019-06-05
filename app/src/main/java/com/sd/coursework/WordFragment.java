package com.sd.coursework;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.sd.coursework.Database.Entity.WordPlus;
import com.sd.coursework.Database.VM.WordViewModel;
import com.sd.coursework.Utils.Adapters.WordAdapter;
import com.sd.coursework.Utils.UI.EmptyFastScrollSupportedRecyclerView;

import java.util.List;

/** NOTICE: some methods annotations have been omitted,
 *  for properly annotated fragment, check {@link CategoryFragment},
 *  as this fragment is conceptually similar
 *  */
public class WordFragment extends Fragment {
    private WordViewModel wordViewModel;
    private WordAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_words, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.word_main_bar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        // Initial setup of recycler view
        EmptyFastScrollSupportedRecyclerView recyclerView = getActivity().findViewById(R.id.word_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new WordAdapter(new WordAdapter.WordAdapterListener() {
            @Override
            public void startWordDetailedFrag(int catId, int wordId) {
                String tag = getResources().getString(R.string.fragment_word_detail_tag);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(tag);
                if (fragment == null) fragment = new WordDetailFragment();

                /* pass the position in a bundle */
                Bundle bundle = new Bundle();
                bundle.putInt("wordId", wordId);
                bundle.putInt("categoryId",catId);  // Not needed, just in case
                fragment.setArguments(bundle);

                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_fade_in, R.anim.slide_fade_out, R.anim.slide_fade_in, R.anim.slide_fade_out)
                        .replace(R.id.main_layout, fragment, tag)
                        .addToBackStack(getResources().getString(R.string.fragment_words_tag))
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);

        // Set Empty view, in case the recycler view is empty
        TextView emptyView = getActivity().findViewById(R.id.word_empty_view);
        recyclerView.setEmptyView(emptyView);

        // bind data view model
        wordViewModel = ViewModelProviders.of(getActivity()).get(WordViewModel.class);
        wordViewModel.getAllWords().observe(this, new Observer<List<WordPlus>>() {
            @Override
            public void onChanged(@Nullable List<WordPlus> words) {
                adapter.setWords(words);
            }
        });


        /*Parent Activity Related UI Changes*/
        getActivity().setTitle("Words");

        // Set current tab in navigation view
        NavigationView menu = getActivity().findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.wordTab);

        // Hide fab when this fragment is shown
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab.isShown()) fab.hide();
    }
}
