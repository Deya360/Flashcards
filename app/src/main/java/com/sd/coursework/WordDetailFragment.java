package com.sd.coursework;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.coursework.API.ResultQuery;
import com.sd.coursework.API.SuggestionFetcher;
import com.sd.coursework.API.Suggestion;
import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Database.VM.WordViewModel;
import com.sd.coursework.Utils.Adapters.SuggestionAdapter;
import com.sd.coursework.Utils.ListenerBoolWrapper;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;

import java.util.ArrayList;

/** NOTICE: some methods annotations have been omitted,
 *  for properly annotated fragments, check {@link CategoryFragment} && {@link CategoryDetailFragment},
 *  as this fragment is conceptually similar
 *  */
public class WordDetailFragment extends Fragment implements View.OnClickListener{
    public static ListenerBoolWrapper nw_editMode;
    final int OPERATION_NEW = 0, OPERATION_UPDATE = 1;

    WordViewModel wordViewModel;
    CategoryViewModel categoryViewModel;
    SuggestionAdapter adapter;
    SuggestionFetcher fetcher;
    boolean expandMenuState = false;
    int operationMode;
    int wordId;
    int categoryId;

    EmptySupportedRecyclerView recyclerView;
    RelativeLayout contentLy, headerLy;
    TextView emptyView;
    EditText wordEt, definitionEt;
    ImageView expandIv;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_word_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Parent Activity Related UI Changes*/
        setupParentUI();

        nw_editMode = new ListenerBoolWrapper();

        //Initial setup of recycler view
        recyclerView = (EmptySupportedRecyclerView)getActivity().findViewById(R.id.nw_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new SuggestionAdapter(getContext(), new SuggestionAdapter.ViewModelListener() {
            @Override
            public void setDefinition(String word, String def) {
                if (nw_editMode.getState()) {
                    if (!wordEt.getText().toString().toLowerCase().equals(word))    /* to avoid removing capitalization of the word */
                        wordEt.setText(word);

                    definitionEt.setText(def);

                } else {
                    Toast.makeText(getContext(), "Can't set definition, not in edit mode", Toast.LENGTH_SHORT).show();
                }

            }
        });
        recyclerView.setAdapter(adapter);

        //Set Loading Progress Bar view, for when data is loading
        ProgressBar emptyProgressView = (ProgressBar)getActivity().findViewById(R.id.nw_empty_progress_view);
        recyclerView.setPlaceHolderView(emptyProgressView);

        //Init views
        expandIv = getActivity().findViewById(R.id.nw_expand_more_lessIv);
        contentLy = getActivity().findViewById(R.id.nw_contentLy);
        emptyView = getActivity().findViewById(R.id.nw_empty_view);

        headerLy = getActivity().findViewById(R.id.nw_headerLy);
        headerLy.setOnClickListener(this);

        wordEt = getActivity().findViewById(R.id.nw_wordEt);

        definitionEt = getActivity().findViewById(R.id.nw_definitionEt);
        definitionEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (wordEt.getText().toString().length() == 0) {
                        wordEt.setError("Can't be empty!");
                        ResultQuery.clear();
                        ResultQuery.postUpdate();

                    } else {
                        if (wordEt.getError()!=null) {
                            wordEt.setError(null);
                        }

                        if (!expandMenuState) {
                            toggleMenuState(true);
                        } else {
                            fetchSuggestions();
                        }
                    }
                }
            }
        });

        nw_editMode.setListener(new ListenerBoolWrapper.ChangeListener() {
            @Override
            public void onChange(boolean state) {
                if (getActivity()!= null) {
                    getActivity().invalidateOptionsMenu();
                }

                if (!state) { //Edit mode InActive
                    if (operationMode==OPERATION_NEW) {
                        if(!wordEt.getText().toString().isEmpty()) {
                            int size = wordViewModel.getAllByCategoryId().getValue().size();
                            wordViewModel.insert(new Word(categoryId,
                                                        size+1,
                                                        wordEt.getText().toString(),
                                                        definitionEt.getText().toString()));

                            categoryViewModel = ViewModelProviders.of(WordDetailFragment.this).get(CategoryViewModel.class);
                            categoryViewModel.updateWordCount(categoryId,1);

                            wordViewModel.getLastId().observe(WordDetailFragment.this, new Observer<Integer>() {
                                @Override
                                public void onChanged(@Nullable Integer integer) {
                                    if (integer!= null) {
                                        wordId = integer;
                                        wordViewModel.getWord(wordId);

                                    }
                                }
                            });

                            wordViewModel.getWord(wordId);

                            operationMode = OPERATION_UPDATE;
                            getActivity().setTitle("Word Detail");
                            Toast.makeText(getContext(), "Word Added", Toast.LENGTH_SHORT).show();

                        } else {
                            getActivity().onBackPressed();
                        }

                    } else {
                        if(!wordEt.getText().toString().isEmpty()) {
                            boolean needsUpdate = false;
                            if (!wordEt.getText().toString().equals(wordViewModel.getWord().getValue().getName())) {
                                wordViewModel.getWord().getValue().setName(wordEt.getText().toString());
                                needsUpdate = true;
                            }
                            if (!definitionEt.getText().toString().equals(wordViewModel.getWord().getValue().getDefinition())) {
                                wordViewModel.getWord().getValue().setDefinition(definitionEt.getText().toString());
                                needsUpdate = true;
                            }

                            if (needsUpdate) { /* Update only if changed, either alone or both fields together */
                                wordViewModel.update(wordViewModel.getWord().getValue());
                                wordViewModel.getWord(wordId);
                                Toast.makeText(getContext(), "Word Updated", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            wordEt.setText(wordViewModel.getWord().getValue().getName());
                            definitionEt.setText(wordViewModel.getWord().getValue().getDefinition());
                        }
                    }
                }

                wordEt.setEnabled(state);
                definitionEt.setEnabled(state);
            }
        });

        // Restore any state (in case of screen rotation)
        if (savedInstanceState!=null){
            nw_editMode.setState(savedInstanceState.getBoolean("nw_editMode"));
            expandMenuState = savedInstanceState.getBoolean("expandMenuState");
            if (expandMenuState) toggleMenuState(true);

            ((FloatingActionButton)getActivity().findViewById(R.id.fab)).hide(); //Hot fix
        }

        // Get passed bundle from parent argument
        if (getArguments()!= null) {
            wordId = getArguments().getInt("wordId");
            categoryId = getArguments().getInt("categoryId");
        }

        // bind data view model
        wordViewModel = ViewModelProviders.of(getActivity()).get(WordViewModel.class);
        wordViewModel.getWord(wordId);
        wordViewModel.getWord().observe(this, new Observer<Word>() {
            @Override
            public void onChanged(@Nullable Word word) {
                if (word!=null && !nw_editMode.getState() && operationMode==OPERATION_UPDATE) {
                    wordEt.setText(word.getName());
                    definitionEt.setText(word.getDefinition());
                }
            }
        });

        if (getArguments()!= null) {
            if (wordId != -1) {
                operationMode = OPERATION_UPDATE;

//                if (savedInstanceState == null) {
//                    wordViewModel.getWord();
//                }
            } else {
                operationMode = OPERATION_NEW;

                if (savedInstanceState == null) {
                    nw_editMode.setState(true);
                    getActivity().setTitle("New Word");
                    ResultQuery.clear();
                    ResultQuery.postUpdate();
                    wordEt.requestFocus();
                }
            }
        }

        ResultQuery.setListener(new ResultQuery.ChangeListener() {
            @Override
            public void onChange(ArrayList<ResultQuery.SummarizedSuggestion> suggestions) {
                setViewAdapter(suggestions);
            }
        });

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab.isShown())
            fab.hide();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nw_headerLy:
                toggleMenuState(!expandMenuState);
                break;
        }
    }

    void toggleMenuState(boolean newState) {
        if (newState) {
            expandIv.setBackgroundResource(R.drawable.ic_expand_less);
            contentLy.setVisibility(View.VISIBLE);
            fetchSuggestions();
        } else {
            expandIv.setBackgroundResource(R.drawable.ic_expand_more);
            contentLy.setVisibility(View.GONE);
        }
        expandMenuState = newState;
    }

    private ActionBar a;
    void setupParentUI() {
        setHasOptionsMenu(true);

        // Set toolbar title
        getActivity().setTitle("Word Detail");

        // Set current tab in navigation view
//        NavigationView menu = getActivity().findViewById(R.id.nav_view);
//        menu.setCheckedItem(R.id.catTab);

        // Hide fab when this fragment is shown
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab.isShown()) fab.hide();
    }

    private void fetchSuggestions() {
        if (!ResultQuery.getQuery().equals(wordEt.getText().toString())
            && !wordEt.getText().toString().trim().equals("")) {
            fetcher = new SuggestionFetcher(new Suggestion.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(Boolean success) {
                    ResultQuery.postUpdate();
                }
            });
            fetcher.execute(wordEt.getText().toString());

        } else {
            setViewAdapter(ResultQuery.getSummarizedSuggestions());
        }
    }

    private void setViewAdapter(ArrayList<ResultQuery.SummarizedSuggestion> suggestions) {
        adapter.setWords(suggestions);
        if (suggestions.size() == 0) {
            recyclerView.setEmptyView(emptyView);

        } else {
            if (getActivity()!=null) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(definitionEt.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("wordId", wordId);
        outState.putInt("categoryId",categoryId);   // Needed only for adding new words
        outState.putBoolean("expandMenuState", expandMenuState);
//        outState.putInt("operationMode", operationMode);
        outState.putBoolean("nw_editMode", nw_editMode.getState());
    }


    @Override
    public void onDestroy() {
        if (fetcher!=null && fetcher.getStatus()== AsyncTask.Status.RUNNING)
            fetcher.cancel(true);

        nw_editMode.clearListeners();
        if (nw_editMode.getState())
            nw_editMode.setState(false);

        super.onDestroy();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.cat_main_bar, menu);

        boolean visible = nw_editMode.getState();
        menu.findItem(R.id.action_edit).setVisible(!visible);
        menu.findItem(R.id.action_apply).setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                nw_editMode.setState(true);
                return true;

            case R.id.action_apply:
                if (operationMode==OPERATION_NEW) {
                    if (wordEt.getText().toString().isEmpty() && !definitionEt.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "New word must have a name", Toast.LENGTH_SHORT).show();

                    } else if (wordEt.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "Addition Cancelled", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();

                    } else {
                        nw_editMode.setState(false);
                    }

                } else {
                    if (!wordEt.getText().toString().isEmpty()) {
                        nw_editMode.setState(false);
                    }
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
