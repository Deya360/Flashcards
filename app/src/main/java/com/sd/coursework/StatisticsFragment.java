package com.sd.coursework;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Database.VM.ResultViewModel;
import com.sd.coursework.Utils.Adapters.StatisticsAdapter;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment{
    private ArrayAdapter<String> spinnerAdapter;
    private CategoryViewModel categoryViewModel;
    private ResultViewModel resultViewModel;
    private int selectedItemIdx = 0;

    EmptySupportedRecyclerView recyclerView;
    private TextView emptyView1, emptyView2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();

        if (savedInstanceState!=null){
            selectedItemIdx = savedInstanceState.getInt("selectedItemIdx");
        }

        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.stat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        StatisticsAdapter adapter = new StatisticsAdapter(new StatisticsAdapter.StatisticsAdapterListener() {
            @Override
            public void startStatisticDetailFrag(int pos) {

            }
        });
        recyclerView.setAdapter(adapter);

        ProgressBar placeHolderView = getActivity().findViewById(R.id.stat_empty_progress_view);
        recyclerView.setPlaceHolderView(placeHolderView);

        emptyView1 = getActivity().findViewById(R.id.stat_empty_view1);
        emptyView2 = getActivity().findViewById(R.id.stat_empty_view2);


        categoryViewModel = ViewModelProviders.of(getActivity()).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if (categories!=null) {
                    List<String> spinnerArray =  new ArrayList<>();
                    spinnerArray.add("Select Category");

                    for (Category category : categories) {
                        spinnerArray.add(category.getName());
                    }

                    spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinnerArray) {
                        /* here we remove the first (default value) row from the dropdown list */
                        @Override
                        public View getDropDownView(int position, @androidx.annotation.Nullable View convertView, @androidx.annotation.NonNull ViewGroup parent) {
                            View v = null;

                            if (position == 0) {
                                TextView tv = new TextView(getContext());
                                tv.setHeight(0);
                                tv.setVisibility(View.GONE);
                                v = tv;

                            } else {
                                v = super.getDropDownView(position, null, parent);
                            }

                            parent.setVerticalScrollBarEnabled(false);
                            return v;
                        }
                    };
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                }
            }
        });

        resultViewModel = ViewModelProviders.of(getActivity()).get(ResultViewModel.class);
        resultViewModel.getAllByCategoryId().observe(this, new Observer<List<Result>>() {
            @Override
            public void onChanged(@Nullable List<Result> results) {
                if (results!=null) {
                    adapter.setResults(results);

                    if(results.size()==0) {
                        recyclerView.setEmptyView(emptyView2);
                    }
                }
            }
        });

    }

    /*Parent Activity Related UI Changes*/
    void setupUI() {
        setHasOptionsMenu(true);

        // Set toolbar title
        getActivity().setTitle("Statistics");

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
        inflater.inflate(R.menu.stats_main_bar, menu);

        Spinner categorySp = (Spinner) menu.findItem(R.id.spinner).getActionView();
        categorySp.setAdapter(spinnerAdapter);

        categorySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
                    int resId = categoryViewModel.getAllCategories().getValue().get(position-1).getId();
                    resultViewModel.getAllByCategoryId(resId);
                    selectedItemIdx = position;

                } else {
                    recyclerView.setEmptyView(emptyView1);
                }
            }
        });

        categorySp.setSelection(selectedItemIdx);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("selectedItemIdx",selectedItemIdx);
        super.onSaveInstanceState(outState);
    }


}
