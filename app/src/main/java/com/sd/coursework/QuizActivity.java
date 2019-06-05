package com.sd.coursework;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.Database.Entity.WordLite;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Database.VM.ResultViewModel;
import com.sd.coursework.Database.VM.WordViewModel;
import com.sd.coursework.Utils.Adapters.QuizAdapter;
import com.sd.coursework.Utils.ListenerIntWrapper;
import com.sd.coursework.Utils.QuizItem;
import com.sd.coursework.Utils.UI.CircleButton;
import com.varunest.sparkbutton.SparkButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;

public class QuizActivity extends AppCompatActivity {
    public static ListenerIntWrapper quiz_FlipCard;
    private final int ANSWER_INDETERMINATE = -1, ANSWER_CORRECT = 1, ANSWER_WRONG = 0;

    private boolean onBackWarned = false;
    private boolean quizBegun;
    private int answerState = ANSWER_INDETERMINATE;
    private int correctCounter =0, wrongCounter =0;
    private int reachedFlag = 0;
    private int vpPos = -1;

    private static List<QuizItem> stats;
    private static List<WordLite> items;
    private int categoryId;
    private QuizAdapter adapter;
    private CategoryViewModel categoryViewModel;

    Spinner wordAmountSp;
    ViewPager viewPager;
    CircleButton wrongBtn, nextBtn;
    SparkButton correctBtn;
    TextView correctCounterTv, wrongCounterTv, wordsLeftTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        overridePendingTransition(R.anim.zoom_enter,0);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Quiz: ");


        if (getIntent().getExtras()!= null) {
            categoryId = getIntent().getExtras().getInt("categoryId");
        }

        //Restore from saved instance state
        if (savedInstanceState != null) {
            categoryId = savedInstanceState.getInt("categoryId", categoryId);
            quizBegun = savedInstanceState.getBoolean("quizBegun",false);

            if (quizBegun) { // just in case
                reachedFlag = savedInstanceState.getInt("reachedFlag");
                correctCounter = savedInstanceState.getInt("correctCounter");
                wrongCounter = savedInstanceState.getInt("wrongCounter");
                answerState = savedInstanceState.getInt("answerState");
                vpPos = savedInstanceState.getInt("vpPos",vpPos);
            }
        } else {
            items = null;
            stats = null;
        }


        Button startBtn = findViewById(R.id.q_startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quizBegun = true;

                initQuiz();
            }
        });

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        categoryViewModel.getById(categoryId);
        categoryViewModel.getById().observe(this, new Observer<Category>() {
            @Override
            public void onChanged(@Nullable Category category) {
                if (category != null) {
                    setTitle("Quiz: " + category.getName());
                    initSpinner(category.getWordCount());
                }
            }
        });

        if (quizBegun) initQuiz();
    }

    private void initQuiz() {
        RelativeLayout contentLy = findViewById(R.id.q_contentLy);
        RelativeLayout setupLy = findViewById(R.id.q_setupLy);

        initWVM();
        setupLy.setVisibility(GONE);
        startFadeUpAnim(contentLy,200);
        initUI();
    }

    private void initSpinner(int totalWordCount) {
        List<Integer> spinnerArray =  new ArrayList<>();

        int counter = totalWordCount;
        int interval;

        // populate spinner with gradually decrementing options, a ratio of 0.3
        while (counter>0) {
            spinnerArray.add(counter);

            if (counter >= 5000) interval = 1500;
            else if  (counter >= 1000) interval = 500;
            else if  (counter >= 500) interval = 150;
            else if  (counter >= 250) interval = 75;
            else if  (counter >= 100) interval = 30;
            else if  (counter >= 50) interval = 15;
            else if  (counter >= 25) interval = 7;
            else interval = 3;

            counter-= interval;
        }

        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        wordAmountSp = findViewById(R.id.q_word_amountSp);
        wordAmountSp.setAdapter(spinnerAdapter);
    }

    private void initUI() {
        // Init info views
        wordsLeftTv = findViewById(R.id.q_words_leftTv);
        wrongCounterTv = findViewById(R.id.q_wrong_counterTv);
        correctCounterTv = findViewById(R.id.q_correct_counterTv);

        //Initialize viewpager and adapter
        viewPager = findViewById(R.id.q_cardVp);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrollStateChanged(int i) {}
            @Override public void onPageScrolled(int i, float v, int i1) {}

            @Override
            public void onPageSelected(int i) {
                if (i==reachedFlag) {
                    setIndeterminate();

                } else {
                    setPageFromStats(i);
                }
            }
        });

        adapter = new QuizAdapter(this, new QuizAdapter.AdapterListener() {
            @Override
            public void notifyFlipped(int position) {
                if (position == reachedFlag && answerState == ANSWER_INDETERMINATE) {
                    setWrong();
                }
            }
        });
        viewPager.setAdapter(adapter);

        quiz_FlipCard = new ListenerIntWrapper();

        nextBtn = findViewById(R.id.q_nextBtn);
        nextBtn.setVisibility(GONE);
        nextBtn.setOnClickListener(v -> {
            if (answerState!=ANSWER_INDETERMINATE) {
                if (reachedFlag!=items.size()) {
                    setIndeterminate();

                } else { // Quiz Complete
                    ResultViewModel resultViewModel = ViewModelProviders.of(this).get(ResultViewModel.class);

                    ArrayList<Integer> correctIds = new ArrayList<>();
                    ArrayList<Integer> wrongIds = new ArrayList<>();
                    for (QuizItem item : stats) {
                        if (item.getOutcome()==ANSWER_CORRECT) correctIds.add(item.getId());
                        else wrongIds.add(item.getId());
                    }

                    Result res = new Result(categoryId,
                            Calendar.getInstance().getTime(),
                            correctIds, wrongIds);

                    resultViewModel.getLastId().observe(QuizActivity.this, new Observer<Integer>() {
                        @Override
                        public void onChanged(@Nullable Integer integer) {
                            if (integer!=null) {
                                categoryViewModel.getById().getValue().setLearnedWordCnt(correctCounter);
                                categoryViewModel.update(categoryViewModel.getById().getValue());

                                startStatisticsFragment(categoryId,integer);

                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        }
                    });

                    resultViewModel.insert(res);
                }

            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
            }

        });

        correctBtn = findViewById(R.id.q_correctBtn);
        correctBtn.setOnClickListener(v -> {
            correctBtn.playAnimation();
            setCorrect();
        });

        wrongBtn = findViewById(R.id.q_wrongBtn);
        wrongBtn.setOnClickListener(v -> {
            quiz_FlipCard.setCount(reachedFlag);
            setWrong();
        });
    }

    private void startStatisticsFragment(int categoryId, int resId) {

    }

    private void setPageFromStats(int pageNum) {
        int outcome = stats.get(pageNum).getOutcome();
        updateButtons(ANSWER_INDETERMINATE);
        updateButtons(outcome);
        toggleButtons(false);
    }

    private void startFadeUpAnim(View view, int startDelay) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.setTranslationY(view.getMeasuredHeight());

        view.animate()
                .setStartDelay(startDelay)
                .translationY(0)
                .alpha(1.0f);
    }

    private void initWVM() {
        CheckBox shuffleCb = findViewById(R.id.q_shuffleCb);

        WordViewModel wordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
        wordViewModel.getAllByCategoryIdLite(categoryId);
        wordViewModel.getAllByCategoryIdLite().observe(this, new Observer<List<WordLite>>() {
            @Override
            public void onChanged(@Nullable List<WordLite> wordLites) {
                if (wordLites != null) {
                    if (items==null) {
                        items = wordLites.subList(0,(int) wordAmountSp.getSelectedItem());;
                        if (shuffleCb.isChecked()) {
                            Collections.shuffle(items);
                        }
                        stats = new ArrayList<>();
                    }

                    adapter.setItems(items.subList(0,reachedFlag+1));
                    updateInfoViews();

                    if (vpPos == -1) {
                        viewPager.setCurrentItem(reachedFlag);

                    } else {
                        //Because Viewpager doesn't call necessary methods when page doesn't change
                        if ((vpPos==viewPager.getCurrentItem()) && (answerState!=ANSWER_INDETERMINATE)) {
                            setPageFromStats(vpPos);

                        } else {
                            viewPager.setCurrentItem(vpPos);
                        }

                    }
                }
            }
        });
    }

    private void updateViewPager() {
        if (items != null) {
            if (reachedFlag+1>items.size()) {
                Toast.makeText(this, "DEBUG: QUIZ DONE", Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.addItem(items.get(reachedFlag));
        }
    }

    private void updateButtons(int outcome) {
        switch (outcome) {
            case ANSWER_CORRECT:
                int defaultColor = Color.parseColor("#99CC00");
                int darkenedColor = ColorUtils.blendARGB(defaultColor, Color.BLACK,0.2f);
                correctBtn.setBackgroundTintList(ColorStateList.valueOf(darkenedColor));

                wrongBtn.setColor(Color.parseColor("#787975"));
                break;

            case ANSWER_WRONG:
                wrongBtn.setColor(wrongBtn.getDisabledColor());
                correctBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#787975")));
                break;

            case ANSWER_INDETERMINATE:
                wrongBtn.setDefaultColor();
                correctBtn.setBackgroundTintList(null);
                break;
        }

    }

    private void updateInfoViews() {
        correctCounterTv.setText(String.valueOf(correctCounter));
        wrongCounterTv.setText(String.valueOf(wrongCounter));
        wordsLeftTv.setText("Words Left: " + (items.size()-reachedFlag));
    }

    private void toggleButtons(boolean state) {
        correctBtn.setEnabled(state);
        wrongBtn.setEnabled(state);

        if (!state) {
            if (stats.size()-1==reachedFlag) {
                startFadeUpAnim(nextBtn,0);

            } else {
                nextBtn.setVisibility(View.VISIBLE);
            }
        } else {
            nextBtn.setVisibility(GONE);
        }
    }


    private void setCorrect() {
        if (answerState==ANSWER_CORRECT) {
            return;
        }

        answerState = ANSWER_CORRECT;
        stats.add(new QuizItem(items.get(reachedFlag).getWordId(), ANSWER_CORRECT));

        reachedFlag++;
        correctCounter++;

        updateButtons(answerState);
        toggleButtons(false);
        updateInfoViews();
        updateViewPager();
    }


    private void setWrong() {
        if (answerState==ANSWER_WRONG) {
            return;
        }

        answerState = ANSWER_WRONG;
        stats.add(new QuizItem(items.get(reachedFlag).getWordId(), ANSWER_WRONG));

        reachedFlag++;
        wrongCounter++;

        updateButtons(answerState);
        toggleButtons(false);
        updateInfoViews();
        updateViewPager();
    }

    private void setIndeterminate() {
        answerState = ANSWER_INDETERMINATE;

        updateButtons(answerState);
        toggleButtons(true);
        viewPager.setCurrentItem(reachedFlag); //ERROR?
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("categoryId",categoryId);
        outState.putBoolean("quizBegun",quizBegun);
        outState.putInt("reachedFlag",reachedFlag);
        outState.putInt("correctCounter",correctCounter);
        outState.putInt("wrongCounter",wrongCounter);
        outState.putInt("answerState",answerState);
        outState.putInt("vpPos",viewPager.getCurrentItem());

        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!onBackWarned && quizBegun) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        onBackWarned = true;
                        onBackPressed();
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(QuizActivity.this);
            builder.setMessage("Abort this quiz?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show();

            return;
        }

        super.onBackPressed();
    }
}
