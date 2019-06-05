package com.sd.coursework.Utils;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.VM.ResultViewModel;
import com.sd.coursework.R;
import com.sd.coursework.Utils.Adapters.StatisticsDetailAdapter;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;

import java.util.List;

public class StatisticsDetailFragment extends Fragment {
    private ResultViewModel resultViewModel;
    private int resultId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_statistics_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();

        if (getArguments()!= null) {
            resultId = getArguments().getInt("resultId");
        }

        if (savedInstanceState!=null){
            resultId = savedInstanceState.getInt("resultId");
        }

        // Initial setup of recycler view
        EmptySupportedRecyclerView recyclerView = getActivity().findViewById(R.id.stat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        StatisticsDetailAdapter adapter = new StatisticsDetailAdapter(new StatisticsDetailAdapter.StatisticsDetailAdapterListener() {
            @Override
            public void startQuizActivity(int categoryId) {

            }

            @Override
            public void startWordDetailedFrag(int catId, int wordId) {

            }
        });
        recyclerView.setAdapter(adapter);

        ProgressBar placeHolderView = getActivity().findViewById(R.id.stat_empty_progress_view);
        recyclerView.setPlaceHolderView(placeHolderView);


        resultViewModel = ViewModelProviders.of(getActivity()).get(ResultViewModel.class);
        resultViewModel.getAllByCategoryId().observe(this, new Observer<List<Result>>() {
            @Override
            public void onChanged(@Nullable List<Result> results) {
                if (results!=null) {
//                    adapter.setResults(results);
                }
            }
        });

    }

    /*Parent Activity Related UI Changes*/
    void setupUI() {
        setHasOptionsMenu(true);

        // Set toolbar title
        getActivity().setTitle("Statistics: Result Detail");

        // Set current tab in navigation view
        NavigationView menu = getActivity().findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.statTab);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab.isShown()) fab.hide();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
//        inflater.inflate(R.menu.stats_main_bar, menu);

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("resultId",resultId);
        super.onSaveInstanceState(outState);
    }

}
