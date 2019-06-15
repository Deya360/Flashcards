package com.sd.coursework;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.VM.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class IntentReceiverActivity extends AppCompatActivity {
    private List<Category> categoriesList;
    private boolean activityShown = false;

    private Spinner categorySp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null) {
            activityShown = savedInstanceState.getBoolean("activityShown");
            if (activityShown) return;
        }

        // if first time
        Intent intent = getIntent();
        if (!intentIsValid(intent)) {
            declareInvalid();
        }

        setTheme(R.style.DialogTheme);
        setContentView(R.layout.activity_intent_reciever);
        setTitle("Add new word");

        //Setup Dialog
        categorySp = findViewById(R.id.dialog_categorySp);

        CategoryViewModel categoryViewModel = ViewModelProviders.of(IntentReceiverActivity.this).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if (categories!=null) {
                    if (categories.size()!=0) {
                        categoriesList = categories;
                        populateSpinner();

                    } else {
                        declareInvalid();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override public void run() {
                                Toast.makeText(IntentReceiverActivity.this, "No categories available!", Toast.LENGTH_LONG).show();
                            }
                        }, 0);
                    }
                }
            }
        });

        Button selectBtn = findViewById(R.id.dialog_selectBtn);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categorySp.getSelectedItem()!=null) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    int categoryId = categoriesList.get(categorySp.getSelectedItemPosition()).getId();

                    startNewWordActivity(sharedText, categoryId);
                    finish();

                } else {
                    declareInvalid();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(IntentReceiverActivity.this, "No category was selected", Toast.LENGTH_LONG).show();
                        }
                    }, 0);
                }
            }
        });
    }

    private boolean intentIsValid(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                return (intent.getStringExtra(Intent.EXTRA_TEXT) != null);
            }
        }
        return false;
    }

    private void declareInvalid() {
        Log.e("IntentReceiverActivity","Intent invalid");
        finish();
    }

    private void populateSpinner() {
        ArrayList<String> categoryNames = new ArrayList<>();
        for (int i = 0; i < categoriesList.size(); i++) {
            categoryNames.add(categoriesList.get(i).getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySp.setAdapter(spinnerAdapter);
    }

    private void startNewWordActivity(String wordName, int categoryId) {
        Intent myIntent = new Intent(this, WordDetailActivity.class);
        myIntent.putExtra("wordName", wordName);
        myIntent.putExtra("categoryId",categoryId);
        startActivity(myIntent);
        activityShown = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("activityShown",activityShown);

        super.onSaveInstanceState(outState);
    }
}
