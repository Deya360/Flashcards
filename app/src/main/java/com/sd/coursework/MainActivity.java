package com.sd.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import static com.sd.coursework.BuildConfig.COPR;
import static com.sd.coursework.CategoryDetailFragment.catdet_editMode;
import static com.sd.coursework.CategoryFragment.cat_editMode;
import static com.sd.coursework.WordDetailFragment.nw_editMode;

public class MainActivity extends AppCompatActivity {
    public static Boolean USE_COLOR_QUIZ_FLASHCARDS = false;
    private int currentNavTab;

    NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup side drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Restore previous selected tab (if available)
        int currentTab;
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt("current_selected_nav_tab");
        } else {
            currentTab = R.id.catTab;   /* Set Default Tab (because it's the default one by design)*/
        }

        // Setup side drawer navigation view & override onclick to initiate various fragments
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switchFragment(menuItem.getItemId());
                currentNavTab = menuItem.getItemId();
                invalidateOptionsMenu();

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        navView.setCheckedItem(currentTab);

        if (savedInstanceState == null) {
            switchFragment(currentTab);
        }


        /* Ignore below, just an easter egg,,,, well, not anymore :<( */
        final String PREFS_NAME = "com.sd.coursework";
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        USE_COLOR_QUIZ_FLASHCARDS = prefs.getBoolean("USE_COLOR_QUIZ_FLASHCARDS",false);
        navView.getHeaderView(0).findViewById(R.id.imageView).setOnLongClickListener(new View.OnLongClickListener() {
            int pressCounter = 0;
            @Override
            public boolean onLongClick(View v) {
                if (pressCounter <4) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    pressCounter++;

                } else {
                    pressCounter = 0;
                    Toast.makeText(MainActivity.this, "Developed by " + COPR, Toast.LENGTH_SHORT).show();


                    final String msg;

                    if (!USE_COLOR_QUIZ_FLASHCARDS) msg = "Colorful quiz flashcards unlocked!";
                    else msg = "Clean quiz flashcards restored!";
                    USE_COLOR_QUIZ_FLASHCARDS = !USE_COLOR_QUIZ_FLASHCARDS;
                    prefs.edit().putBoolean("USE_COLOR_QUIZ_FLASHCARDS",USE_COLOR_QUIZ_FLASHCARDS).apply();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }, 2000);
                }

                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_selected_nav_tab", currentNavTab);
    }


    /*
    * e.i: Fragment Manager: helper method (helps easily switch between different fragments)
    *       - organizes the back stack of the application and keeps no duplicate fragments in back stack
    * */
    private void switchFragment(int menu_id) {
        FragmentManager manager = getSupportFragmentManager();
        int tagI = 0;

        switch (menu_id) {
            case R.id.catTab:
                tagI = R.string.fragment_categories_tag;
                break;

            case R.id.wordTab:
                tagI = R.string.fragment_words_tag;
                break;

            case R.id.statTab:
                tagI = R.string.fragment_statistics_tag;
                break;
        }

        if (tagI != 0) {
            String tagS = getString(tagI);
            Fragment fragment = manager.findFragmentByTag(tagS);

            if (fragment==null) {
                switch (tagI) {
                    case R.string.fragment_categories_tag:
                        fragment = new CategoryFragment();
                        break;

                    case R.string.fragment_words_tag:
                        fragment = new WordFragment();
                        break;

                    case R.string.fragment_statistics_tag:
                        fragment = new StatisticsFragment();
                        break;
                }
                manager.popBackStackImmediate (null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            } else {
                manager.popBackStackImmediate (tagS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            manager.beginTransaction()
                    .replace(R.id.main_layout, fragment, tagS)
                    .commit();
        }
    }

    /**
    * The function allows the user to exit edit mode by back press even when items are selected,
    * without it, default behaviour will only close the edit action mode bar alone
    * */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;

        } else if (nw_editMode!=null && nw_editMode.getState()) {
            nw_editMode.setState(false);
            return;

        } else if (catdet_editMode!=null && catdet_editMode.getState()) {
            catdet_editMode.setState(false);
            return;

        } else if (cat_editMode!=null && cat_editMode.getState()) {
            cat_editMode.setState(false);
            return;
        }
        super.onBackPressed();
    }
}
