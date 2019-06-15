package com.sd.coursework;

import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.Entity.ResultLite;
import com.sd.coursework.Database.Entity.WordLite;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Database.VM.ResultViewModel;
import com.sd.coursework.Database.VM.WordViewModel;
import com.sd.coursework.Utils.Adapters.StatisticsDetailAdapter;
import com.sd.coursework.Utils.QuizFeedback;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class StatisticsDetailFragment extends Fragment {
    private static final int QUIZ_ACTIVITY_RQ = 20;

    private ResultViewModel resultViewModel;
    private WordViewModel wordViewModel;

    private ArrayList<Pair<Integer,Boolean>> results;
    private ArrayList<String> resultsRaw;
    private boolean showRetestAction = false;
    private boolean isFirstTime = true;
    private String feedbackMsg = "";
    private Result currentResult;
    private Result previousResult;
    private int resultId;

    MenuItem actionRestart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_statistics_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity()==null) {
            Log.e("StatisticsDetailFrag", "Activity is null onViewCreated");
            return;
        }

        if (getArguments()!= null) {
            resultId = getArguments().getInt("resultId",-1);
            feedbackMsg = getArguments().getString("feedbackMsg","");
            if (!feedbackMsg.equals("")) {
                Toast.makeText(getContext(), feedbackMsg, Toast.LENGTH_LONG).show();
            }
            setArguments(null);
        }

        if (savedInstanceState!=null){
            isFirstTime = false;
            resultId = savedInstanceState.getInt("resultId",-1);
            resultsRaw = savedInstanceState.getStringArrayList("resultsRaw");
            feedbackMsg = savedInstanceState.getString("feedbackMsg");
            showRetestAction = savedInstanceState.getBoolean("showRetestAction");
        }

        if (resultId==-1) {
            Log.e("StatisticsDetailFrag", "Result id was not set");
            return;
        }

        setupUI();

        // Initial setup of recycler view
        EmptySupportedRecyclerView recyclerView = getActivity().findViewById(R.id.statdet_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        StatisticsDetailAdapter adapter = new StatisticsDetailAdapter(new StatisticsDetailAdapter.StatisticsDetailAdapterListener() {
            @Override
            public void startWordDetailedFrag(int wordId) {
                String tag = getResources().getString(R.string.fragment_word_detail_tag);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(tag);
                if (fragment == null) fragment = new WordDetailFragment();

                /* pass the position in a bundle */
                Bundle bundle = new Bundle();
                bundle.putInt("wordId", wordId);
//                bundle.putInt("categoryId",currentResult.getCategoryId());  // Not needed, just in case
                fragment.setArguments(bundle);

                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_fade_in, R.anim.slide_fade_out, R.anim.slide_fade_in, R.anim.slide_fade_out)
                        .replace(R.id.main_layout, fragment, tag)
                        .addToBackStack(getResources().getString(R.string.fragment_statistics_detail_tag))
                        .commit();
            }
        });
        recyclerView.setAdapter(adapter);

        ProgressBar placeHolderView = getActivity().findViewById(R.id.statdet_empty_progress_view);
        recyclerView.setPlaceHolderView(placeHolderView);

        wordViewModel = ViewModelProviders.of(getActivity()).get(WordViewModel.class);
        wordViewModel.getWords().observe(this, new Observer<List<WordLite>>() {
            @Override
            public void onChanged(@Nullable List<WordLite> wordLites) {
                if (wordLites!=null && resultsRaw!=null) {
                    List<ResultLite> adapterList = new ArrayList<>();

                    results = new ArrayList<>();
                    String[] splitArr;
                    for (String str : resultsRaw) {
                        splitArr = str.split(":");
                        results.add(new Pair<>(Integer.parseInt(splitArr[1]), splitArr[0].equals("C")));
                    }

                    //this is required to match the results array and the returned words array, and handle cases where the
                    // words corresponding to the word id's in the result have been deleted
                    for (int i = 0, j = 0; (i < results.size()) && (j < wordLites.size()); i++) {
                        ResultLite resultLite;
                        if (results.get(i).first == wordLites.get(j).getWordId()) {
                            resultLite = new ResultLite(wordLites.get(j).getWordId(), wordLites.get(j).getWordName(),results.get(i).second);
                            j++;

                        } else {
                            resultLite = new ResultLite(-1, "#deleted",results.get(i).second);
                        }
                        adapterList.add(resultLite);
                    }

                    if (adapterList.size()==0) {
                        for (int i = 0; i < results.size(); i++) {
                            adapterList.add(new ResultLite(-1, "#deleted",results.get(i).second));

                        }
                    }

                    adapter.setResults(adapterList);
                }
            }
        });

        resultViewModel = ViewModelProviders.of(getActivity()).get(ResultViewModel.class);
        resultViewModel.getPairResult().observe(this, new Observer<List<Result>>() {
            @Override
            public void onChanged(@Nullable List<Result> results) {
                if (results!=null) {
                    if (results.size()==1) {
                        currentResult = results.get(0);

                    } else if (results.get(0).getId() == resultId) {
                        currentResult = results.get(0);
                        previousResult = results.get(1);

                    } else if (results.get(1).getId() == resultId) { //Just in case, ideally the record is always going to be first in the returned arr.
                        currentResult = results.get(1);
                        previousResult = results.get(0);

                    } else {
                        Log.w("StatisticsDetailFrag", "getPairResult:onChanged: No record returned matches needed results Id");
                        return;
                    }
                    resultsRaw = currentResult.getWordIds();

                    ArrayList<Integer> wordIds = new ArrayList<>();
                    String[] splitArr;
                    for (String str : resultsRaw) {
                        splitArr = str.split(":");
                        wordIds.add(Integer.parseInt(splitArr[1]));
                    }
                    wordViewModel.getWords(wordIds);

                    setupTopCard(currentResult);
                    if (actionRestart!=null && currentResult.getWrongWordCnt()!=0) {
                        showRetestAction = true;
                        actionRestart.setVisible(true);
                    }
                }
            }
        });

        if (isFirstTime) {
            resultViewModel.getPairResult(resultId);
        }
    }

    void setupTopCard(Result result) {
        if (getActivity()==null) {
            Log.e("StatisticsDetailFrag", "Activity is null on setupTopCard");
            getActivity().onBackPressed();
//            throw new NullPointerException("Activity is null on setupTopCard");
        }

        TextView dateTv = getActivity().findViewById(R.id.statdet_dateTv);
        TextView scoreTv = getActivity().findViewById(R.id.statdet_scoreTv);
        ProgressBar progressBar = getActivity().findViewById(R.id.statdet_progressBar);
        TextView progressTv = getActivity().findViewById(R.id.statdet_progressTv);
        TextView currentIndicatorTv = getActivity().findViewById(R.id.statdet_current_indicatorTv);
        TextView currentLabelTv = getActivity().findViewById(R.id.statdet_current_labelTv);

        SimpleDateFormat dateFormat =  new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.getDefault());
        dateTv.setText(dateFormat.format(result.getTakenTS()));

        String totalWords = result.getCorrectWordCnt() + "/" + result.getWordIds().size();
        scoreTv.setText(totalWords);

        int percent = (int)((double)result.getCorrectWordCnt()/result.getWordIds().size()*100);
        progressBar.setProgress(percent);

        if (feedbackMsg.equals("")) feedbackMsg = new QuizFeedback().getRandomSnark(percent);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), feedbackMsg, Toast.LENGTH_SHORT).show();
            }
        });
        progressTv.setText(percent + "%");

        currentIndicatorTv.setText(String.valueOf(result.getCorrectWordCnt()));
        if (result.getCorrectWordCnt()==1) currentLabelTv.setText("WORD");

        int visibility = View.GONE;
        if (previousResult!=null) {
            TextView previousIndicatorTv = getActivity().findViewById(R.id.statdet_previous_indicatorTv);
            ImageView previousIndicatorIv = getActivity().findViewById(R.id.statdet_previous_indicatorIv);

            int diff = result.getCorrectWordCnt()-previousResult.getCorrectWordCnt();
            previousIndicatorTv.setText(String.valueOf(Math.abs(diff)));

            int imageRes;
            if (diff > 0) imageRes = R.drawable.icon_index_up;
            else if (diff < 0) imageRes = R.drawable.icon_index_down;
            else imageRes = R.drawable.icon_index_unchanged;

            previousIndicatorIv.setImageResource(imageRes);
            visibility = View.VISIBLE;
        }

        LinearLayout previousIndicatorLy = getActivity().findViewById(R.id.statdet_previous_indicatorLy);
        previousIndicatorLy.setVisibility(visibility);
    }

    /*Parent Activity Related UI Changes*/
    void setupUI() {
        setHasOptionsMenu(true);

        // Set toolbar title
        getActivity().setTitle("Statistics: Quiz Detail");

        // Set current tab in navigation view
        NavigationView menu = getActivity().findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.statTab);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.statdet_main_bar, menu);
        actionRestart = menu.findItem(R.id.action_retest);

        if (showRetestAction) actionRestart.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_delete2) {
            CategoryViewModel categoryViewModel = ViewModelProviders.of(getActivity()).get(CategoryViewModel.class);
            categoryViewModel.getById().observe(this, new Observer<Category>() {
                @Override
                public void onChanged(@Nullable Category category) {
                    if (category!=null) {
                        resultViewModel.delete(currentResult);
                        if (previousResult!=null) {
                            category.setLearnedWordCnt(previousResult.getCorrectWordCnt());

                        } else {
                            category.setLearnedWordCnt(0);
                        }
                        categoryViewModel.update(category);
                        categoryViewModel.getById(currentResult.getCategoryId());

                        getActivity().onBackPressed();
                    }
                }
            });
            categoryViewModel.getById(currentResult.getCategoryId());
            return true;

        } else if (item.getItemId()==R.id.action_retest) {
            ArrayList<Integer> wrongWordsArr = new ArrayList<>();
            for (Pair<Integer, Boolean> res: results){
                if (!res.second) wrongWordsArr.add(res.first);
            }

            Intent myIntent = new Intent(getActivity(), QuizActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();
            myIntent.putExtra("categoryId",currentResult.getCategoryId());
            myIntent.putExtra("wrongWordsArr",wrongWordsArr);
            startActivityForResult(myIntent, QUIZ_ACTIVITY_RQ,bundle);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QUIZ_ACTIVITY_RQ) {
            if (resultCode == RESULT_OK) {
                if (data!=null) {
                    int resId = data.getIntExtra("resId",-1);
                    String feedbackMsg = data.getStringExtra("feedbackMsg");

                    if (resId!=-1) {  //Restart this fragment, showing the new result
                        restartFragment(resId, feedbackMsg);
                    }
                }
            }
        }
    }

    private void restartFragment(int resId, String feedbackMsg) {
        String tag = getResources().getString(R.string.fragment_statistics_detail_tag);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment!=null) {
            Bundle bundle = new Bundle();
            bundle.putInt("resultId", resId);
            bundle.putString("feedbackMsg", feedbackMsg);

            fragment.setArguments(bundle);
            showRetestAction = false;
            resultsRaw = null;

            manager.beginTransaction()
                    .detach(fragment)
                    .attach(fragment)
                    .commit();

        } else {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("resultId",resultId);
        outState.putStringArrayList("resultsRaw",resultsRaw);
        outState.putString("feedbackMsg",feedbackMsg);
        outState.putBoolean("showRetestAction",showRetestAction);
        super.onSaveInstanceState(outState);
    }

}
