package com.sd.coursework;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
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
import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Database.VM.WordViewModel;
import com.sd.coursework.Utils.Adapters.QuerySuggestionAdapter;
import com.sd.coursework.Utils.Adapters.SuggestionAdapter;
import com.sd.coursework.Utils.InternetAccessCheck;
import com.sd.coursework.Utils.ListenerBoolWrapper;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;

import java.util.ArrayList;
import java.util.List;

/** NOTICE: some methods annotations have been omitted,
 *  for properly annotated fragments, check {@link CategoryFragment} && {@link CategoryDetailFragment},
 *  as this fragment is conceptually similar
 *  */
public class WordDetailFragment extends Fragment implements View.OnClickListener{
    public static boolean updateNeeded = false; // Hot fix
    public static ListenerBoolWrapper nw_editMode;
    final int OPERATION_NEW = 0, OPERATION_UPDATE = 1;

    WordViewModel wordViewModel;
    CategoryViewModel categoryViewModel;
    SuggestionAdapter adapter;
    QuerySuggestionAdapter queryAdapter;
    SuggestionFetcher fetcher;
    boolean expandMenuState = false;
    boolean shouldShowAddAnotherButton = true;
    boolean preSetWordName = false;
    String wordName;
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
//        recyclerView.setAdapter(adapter);

        queryAdapter = new QuerySuggestionAdapter(new QuerySuggestionAdapter.QuerySuggestionAdapterListner() {
            @Override
            public void setWord(String word) {
                wordEt.setText(word);
                recyclerView.setAdapter(adapter);
                loadSuggestions();
            }
        });

        //Set Loading Progress Bar view, for when data is loading
        ProgressBar emptyProgressView = getActivity().findViewById(R.id.nw_empty_progress_view);
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
                            loadSuggestions();
                        }
                    }
                }
            }
        });

        nw_editMode.setListener(new ListenerBoolWrapper.ChangeListener() {
            @Override
            public void onChange(boolean state) {
                if (getActivity()!= null && !preSetWordName) {
                    getActivity().invalidateOptionsMenu();
                }

                if (!state) { //Edit mode InActive
                    if (operationMode==OPERATION_NEW) {
                        if(!wordEt.getText().toString().isEmpty()) {

                            wordViewModel.getAllByCategoryId().observe(WordDetailFragment.this, new Observer<List<Word>>() {
                                @Override
                                public void onChanged(@Nullable List<Word> words) {
                                    if (words!=null) {
                                        int size = words.size();
                                        wordViewModel.insert(new Word(categoryId,
                                                size+1,
                                                wordEt.getText().toString(),
                                                definitionEt.getText().toString()));
                                        wordViewModel.getAllByCategoryId().removeObservers(WordDetailFragment.this);
                                    }
                                }
                            });
                            wordViewModel.getAllByCategoryId(categoryId);

                            categoryViewModel = ViewModelProviders.of(WordDetailFragment.this).get(CategoryViewModel.class);
                            categoryViewModel.updateWordCount(categoryId,1);

                            operationMode = OPERATION_UPDATE;
                            if (!wordViewModel.getLastId().hasActiveObservers()) {
                                wordViewModel.getLastId().observe(WordDetailFragment.this, new Observer<Integer>() {
                                    @Override
                                    public void onChanged(@Nullable Integer integer) {
                                        if (integer!= null) {
                                            wordId = integer;
                                            wordViewModel.getWord(wordId);

                                            if (preSetWordName) {
                                                updateNeeded = true;
                                                getActivity().onBackPressed();
//                                                categoryViewModel.getById().observe(WordDetailFragment.this, new Observer<Category>() {
//                                                    @Override
//                                                    public void onChanged(@Nullable Category category) {
//                                                        if (category!= null) {
//
//                                                        }
//                                                    }
//                                                });
//                                                categoryViewModel.getById(categoryId);
                                            }
                                        }
                                    }
                                });
                            }

                            if (!preSetWordName) {
                                getActivity().setTitle("Word Detail");
                                toggleMenuState(false);
                                Toast.makeText(getContext(), "Word Added", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getContext(), "Word Added", Toast.LENGTH_LONG).show();
                                return;
                            }

                        } else {
//                            if (savedInstanceState==null) { //TODO FIX BUG
                                getActivity().onBackPressed();
//                            }
                        }

                    } else {
                        if (operationMode==OPERATION_UPDATE) {
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

                                if (needsUpdate) { /* Update only if changed, either alone separately or both fields together */
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
                }

                wordEt.setEnabled(state);
                definitionEt.setEnabled(state);
            }
        });

        // Get passed bundle from parent argument
        if (getArguments()!= null) {
            wordId = getArguments().getInt("wordId");
            categoryId = getArguments().getInt("categoryId");
            shouldShowAddAnotherButton = getArguments().getBoolean("shouldShowAddAnotherButton");
            wordName = getArguments().getString("wordName");

            if (wordName!=null) preSetWordName = true;
        }

        // bind data view model
        wordViewModel = ViewModelProviders.of(getActivity()).get(WordViewModel.class);
        wordViewModel.getWord().observe(this, new Observer<Word>() {
            @Override
            public void onChanged(@Nullable Word word) {
                if (word!=null && !nw_editMode.getState() && operationMode==OPERATION_UPDATE) {
                    wordEt.setText(word.getName());
                    definitionEt.setText(word.getDefinition());
                }
            }
        });
        wordViewModel.getWord(wordId);


        // Restore any state (in case of screen rotation)
        if (savedInstanceState!=null){
            operationMode = savedInstanceState.getInt("operationMode");
            nw_editMode.setState(savedInstanceState.getBoolean("nw_editMode"));
            expandMenuState = savedInstanceState.getBoolean("expandMenuState");
            if (expandMenuState) toggleMenuState(true);
            shouldShowAddAnotherButton = savedInstanceState.getBoolean("shouldShowAddAnotherButton");
            preSetWordName = savedInstanceState.getBoolean("preSetWordName");
        }


        if (getArguments()!= null) {
            if (wordId != -1) {
                operationMode = OPERATION_UPDATE;

            } else {
                operationMode = OPERATION_NEW;

                if (savedInstanceState == null) {
                    prepareForAddition();
                }
            }
        }

        ResultQuery.setListener(new ResultQuery.ChangeListener() {
            @Override
            public void onChange(ArrayList<ResultQuery.SummarizedSuggestion> suggestions) {
                setViewAdapter(suggestions);
            }

            @Override
            public void onQueryChange(ArrayList<String> querySuggestions) {
                setQueryViewAdapter(querySuggestions);
            }
        });
    }

    private void prepareForAddition() {
        nw_editMode.setState(true);
        getActivity().setTitle("New Word");
        ResultQuery.clear();
        ResultQuery.postUpdate();
        if (preSetWordName) {
            wordEt.setText(wordName);
            definitionEt.requestFocus();

        } else {
            wordEt.requestFocus();
            showKeyboard();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nw_headerLy) {
            toggleMenuState(!expandMenuState);
        }
    }

    void toggleMenuState(boolean newState) {
        if (newState) {
            expandIv.setBackgroundResource(R.drawable.ic_expand_less);
            contentLy.setVisibility(View.VISIBLE);
            loadSuggestions();
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

        // Hide fab when this fragment is shown
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab!=null) fab.hide(); //Required when using this fragment through Intent
    }

    private void loadSuggestions() {
        /* we circumvent unnecessary data fetches, by only processing new fetch requests*/
        if (!ResultQuery.getQuery().equalsIgnoreCase(wordEt.getText().toString())
            && !wordEt.getText().toString().trim().equals("")) {

            if (getActivity()!=null) {
                // Check for network connection
                ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                // Network connection maybe available, however there may not be any connection,
                // so, If network connection is available, then check if network accesses is available too
                //TODO: UNCOMMENT THE BELOW FOR FINAL RELEASE
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
//                    new InternetAccessCheck(internet -> {
                        //Callback
//                        if (internet) {
                            fetchSuggestions();

//                        } else {
//                            Toast.makeText(getContext(), "Can't connect to the server", Toast.LENGTH_LONG).show();
//                        }
//                    });
                    return;
                } else {
                    Toast.makeText(getContext(), "Can't connect to the Internet", Toast.LENGTH_LONG).show();
                }
            }
        }

        if (!ResultQuery.isIsWordASuggestion()) {
            setViewAdapter(ResultQuery.getSummarizedSuggestions());
        } else {
            setQueryViewAdapter(ResultQuery.getQuerySuggestions());
        }
    }

    private void fetchSuggestions () {
        fetcher = new SuggestionFetcher(new Suggestion.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(Boolean success) {
                ResultQuery.postUpdate();
            }
        });
        recyclerView.setAdapter(null);
        ProgressBar emptyProgressView = getActivity().findViewById(R.id.nw_empty_progress_view);
        recyclerView.setPlaceHolderView(emptyProgressView);
        fetcher.execute(wordEt.getText().toString());
    }

    private void setViewAdapter(ArrayList<ResultQuery.SummarizedSuggestion> suggestions) {
        recyclerView.setAdapter(adapter);
        adapter.setWords(suggestions);
        if (suggestions.size() == 0) {
            recyclerView.setEmptyView(emptyView);

        } else {
            hideKeyboard();
        }
    }

    private void setQueryViewAdapter(ArrayList<String> querySuggestions) {
        recyclerView.setAdapter(queryAdapter);
        queryAdapter.setWords(querySuggestions);
        if (querySuggestions.size() == 0) {
            recyclerView.setEmptyView(emptyView);

        } else {
            hideKeyboard();
        }
    }

    private void showKeyboard() {
        if (getActivity()!=null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(wordEt, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        if (getActivity()!=null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(definitionEt.getWindowToken(), 0);
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("wordId", wordId);
        outState.putInt("categoryId",categoryId);   // Needed only for adding new words
        outState.putInt("operationMode",operationMode);
        outState.putBoolean("expandMenuState", expandMenuState);
        outState.putBoolean("nw_editMode", nw_editMode.getState());
        outState.putBoolean("shouldShowAddAnotherButton",shouldShowAddAnotherButton);
        outState.putBoolean("preSetWordName",preSetWordName);
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
        inflater.inflate(R.menu.worddet_main_bar, menu);

        boolean visible = nw_editMode.getState();
        menu.findItem(R.id.action_edit).setVisible(!visible);
        menu.findItem(R.id.action_apply).setVisible(visible);

        if (shouldShowAddAnotherButton) {
            menu.findItem(R.id.action_add).setVisible(!visible);

        } else {
            menu.findItem(R.id.action_add).setVisible(false);
        }
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

            case R.id.action_add:
                operationMode = OPERATION_NEW;
                wordId = -1;
                prepareForAddition();
                wordEt.setText("");
                definitionEt.setText("");

        }

        return super.onOptionsItemSelected(item);
    }
}
