package com.sd.coursework;

import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TooltipCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Database.VM.WordViewModel;
import com.sd.coursework.Utils.Adapters.CategoryDetailAdapter;
import com.sd.coursework.Utils.BckColors;
import com.sd.coursework.Utils.ListenerBoolWrapper;
import com.sd.coursework.Utils.ListenerIntWrapper;
import com.sd.coursework.Utils.Dialogs.ColorPickerDialog;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;
import com.sd.coursework.Utils.UI.ItemMoveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/** NOTICE: some methods annotations have been omitted,
 *  for properly annotated fragment, check {@link CategoryFragment},
 *  as this fragment is conceptually similar
 *  */
public class CategoryDetailFragment extends Fragment{
    private static final int QUIZ_ACTIVITY_RQ = 20;
    public static ListenerBoolWrapper catdet_editMode;

    public static ListenerIntWrapper catdet_selectedItems;
    WordViewModel wordViewModel;
    CategoryViewModel categoryViewModel;

    private ItemTouchHelper touchHelper;
    private ActionMode editActionMode;
    private FloatingActionButton fab;
    private RelativeLayout progressLy;
    private ProgressBar progressBar;
    private EmptySupportedRecyclerView recyclerView;
    private EditText nameEt, descEt;

    private int currentId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_category_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupParentUI();

        catdet_editMode = new ListenerBoolWrapper();
        catdet_selectedItems = new ListenerIntWrapper();

        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.catdet_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        CategoryDetailAdapter adapter = new CategoryDetailAdapter(new CategoryDetailAdapter.StartDragListener() {
            @Override
            public void requestDrag(RecyclerView.ViewHolder viewHolder) {
                touchHelper.startDrag(viewHolder);
            }
        }, new CategoryDetailAdapter.CategoryDetailListener() {
            @Override
            public void updateItemOrder(List<Word> liveList) {
                SparseIntArray changeMap = new SparseIntArray();

                //update list positions of items in database
                for (int i = 0; i < liveList.size(); i++) {
                    //update only if changed
                    if (liveList.get(i).getListPosition() != i + 1) {
                        changeMap.put(liveList.get(i).getId(), i + 1);
                    }
                }
                if (changeMap.size() != 0) {
                    wordViewModel.updateListPositions(changeMap);
                    wordViewModel.getAllByCategoryId(currentId);
                }
            }

            @Override
            public void updateSelectedItems(List<Word> liveList) {
                List<Integer> list = new ArrayList<>();

                for (int i = 0; i < liveList.size(); i++) {
                    if (liveList.get(i).isChecked()) {
                        list.add(liveList.get(i).getId());
                    }
                }
                catdet_selectedItems.setList(list);
                catdet_selectedItems.setMaxSize(liveList.size());
            }

            @Override
            public void startWordDetailFragment(int wordId) {
                startWordDetailFrag(wordId);
            }
        });
        recyclerView.setAdapter(adapter);

        // Set Empty view, in case the recycler view is empty
        ProgressBar emptyView = (ProgressBar)getActivity().findViewById(R.id.catdet_empty_progress_view);
        recyclerView.setPlaceHolderView(emptyView);

        // setup item drag and drop handlers/helpers
        ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // setup progress button
        progressLy = getActivity().findViewById(R.id.catdet_progressLy);
        progressBar = getActivity().findViewById(R.id.catdet_progressBar);
        progressLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryViewModel.getById().getValue().getWordCount() < 1) {
                    Toast.makeText(getContext(), "You need to add some words first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent myIntent = new Intent(getActivity(), QuizActivity.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();
                myIntent.putExtra("categoryId",currentId);
                startActivityForResult(myIntent, QUIZ_ACTIVITY_RQ,bundle);
//                startActivity(myIntent,bundle);
            }
        });

        progressLy.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Category category = categoryViewModel.getById().getValue();
                if (category==null || categoryViewModel.getById().getValue().getWordCount() < 1) {
                    return false;
                }

                float percent = ((float)category.getLearnedWordCnt()/category.getWordCount())*100;
                String text = "Progress: " + category.getLearnedWordCnt() + "/" + category.getWordCount()
                                + " (" + String.format(Locale.getDefault(),"%.2f",percent) + "%)";

                Toast toast= Toast.makeText(getContext(), text, Toast.LENGTH_LONG);

                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 525);
                toast.show();

                return true;
            }
        });

        // bind data view model
        wordViewModel = ViewModelProviders.of(getActivity()).get(WordViewModel.class);
        wordViewModel.getAllByCategoryId().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(@Nullable List<Word> words) {
                if (words != null) {
                    adapter.setWords(words);
                    if (words.size()==0) {
                        TextView emptyView = getActivity().findViewById(R.id.catdet_empty_view);
                        recyclerView.setEmptyView(emptyView);
                    }
                }
            }
        });

        if (getArguments()!= null) {
            // get position, e.i category_id from bundle (passed over from previous fragment)
            currentId = getArguments().getInt("categoryId");

            /* checking for savedInstanceState is very important, as it prevents querying
                data from the database (which repopulates the LiveList with new 'Word' objects
                causing checkbox data and list positions stored inside the object to be lost)
                on events like screen rotations and such */
            if (savedInstanceState == null) {
                wordViewModel.getAllByCategoryId(currentId);
                categoryViewModel.getById(currentId);
            }
        }

        setupCategoryCard();

        //Initialize static variables and Setup their listeners :
        catdet_editMode.setListener(new ListenerBoolWrapper.ChangeListener() {
            @Override
            public void onChange(boolean state) {
                RelativeLayout.LayoutParams nameParams = (RelativeLayout.LayoutParams)nameEt.getLayoutParams();
                RelativeLayout.LayoutParams descParams = (RelativeLayout.LayoutParams)descEt.getLayoutParams();

                nameEt.setEnabled(state);
                descEt.setEnabled(state);

                if (state) {
                    getActivity().setTitle("Category Detail: Edit");
                    fab.hide();

                    // Set Edit Texts to have underline
                    nameEt.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    descEt.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    // Adjust Edit Texts margins
                    nameParams.setMargins(0,(int)getResources().getDimension(R.dimen.name_edit_text_top_margin_enabled),0,0);
                    descParams.setMargins(0,(int)getResources().getDimension(R.dimen.desc_edit_text_top_margin_enabled),0,0);

                    if (descEt.getHint()==null) {
                        descEt.setHint("Description");
                    }

                } else {
                    getActivity().setTitle("Category Detail");
                    fab.show();
                    nameEt.getBackground().clearColorFilter();
                    descEt.getBackground().clearColorFilter();
                    nameParams.setMargins(0,(int)getResources().getDimension(R.dimen.name_edit_text_top_margin_disabled),0,0);
                    descParams.setMargins(0,(int)getResources().getDimension(R.dimen.desc_edit_text_top_margin_disabled),0,0);

                    // Update category information if needed
                    updateCategoryCard();
                }

                nameEt.setLayoutParams(nameParams);
                descEt.setLayoutParams(descParams);

                // Force toolbar redraw
                getActivity().invalidateOptionsMenu();

                // Hide Keyboard
                ((InputMethodManager)getContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        catdet_selectedItems.setListener(new ListenerIntWrapper.ChangeListener() {
            @Override
            public void onChange(int selected_count) {
                if (selected_count > 0) {
                    if (editActionMode == null) {
                        Activity activity = getActivity();
                        editActionMode = getActivity().startActionMode(mActionModeCallBack);
                    }
                    editActionMode.setTitle(String.valueOf(selected_count).concat(" Selected"));

                } else if (editActionMode != null) {
                    editActionMode.finish();
                }

            }
        });

        // Listen for delete event
        catdet_selectedItems.setListener(new ListenerIntWrapper.DeleteListener() {
            @Override
            public void onDelete(List<Integer> itemsList) {
                wordViewModel.deleteDiscrete(itemsList);
                wordViewModel.getAllByCategoryId(currentId);
                categoryViewModel.updateWordCount(currentId,-1*itemsList.size());

                Category category = categoryViewModel.getById().getValue();
                if (category!=null) { /* This ensures we don't get test percentage of over 100 */
                    if (category.getWordCount() < category.getLearnedWordCnt()) {
                        category.setLearnedWordCnt(category.getLearnedWordCnt());
                        categoryViewModel.update(category);
                    }
                }
                categoryViewModel.getById(currentId);

                catdet_editMode.setState(false);
                Toast.makeText(view.getContext(), itemsList.size() + " Item" +((itemsList.size()==1)? "":"s")+ " deleted",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startWordDetailFrag(int id) {
        String tag = getResources().getString(R.string.fragment_word_detail_tag);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment == null) fragment = new WordDetailFragment();

        /* pass the position in a bundle */
        Bundle bundle = new Bundle();
        bundle.putInt("wordId", id);
        bundle.putInt("categoryId",currentId);
        fragment.setArguments(bundle);

        manager.beginTransaction()
                .setCustomAnimations(R.anim.slide_fade_in, R.anim.slide_fade_out, R.anim.slide_fade_in, R.anim.slide_fade_out)
                .replace(R.id.main_layout, fragment, tag)
                .addToBackStack(getResources().getString(R.string.fragment_categories_detail_tag))
                .commit();
    }

    private void updateCategoryCard() {
        if (categoryViewModel.getById().getValue() != null) {   /* Just so compiler stops throwing warnings*/
            // Check if edit texts value was validated
            if (nameEt.getError()==null) {
                //In case the screen was rotated and the fields, didn't have time to populate
                if (!nameEt.getText().toString().equals("")) {
                    boolean needsUpdate = false;
                    if (!nameEt.getText().toString().equals(categoryViewModel.getById().getValue().getName())) {
                        categoryViewModel.getById().getValue().setName(nameEt.getText().toString());
                        needsUpdate = true;
                    }
                    if (!descEt.getText().toString().equals(categoryViewModel.getById().getValue().getDesc())) {
                        categoryViewModel.getById().getValue().setDesc(descEt.getText().toString());
                        needsUpdate = true;
                    }

                    if (needsUpdate) { /* Update only if changed, either alone or both fields together */
                        categoryViewModel.update(categoryViewModel.getById().getValue());
                        categoryViewModel.getById(currentId);
                    }
                }

            } else {
                // Since we are exiting edit mode, we restore valid information to be displayed
                nameEt.setText(categoryViewModel.getById().getValue().getName());
                nameEt.setError(null);
            }

            descEt.setHint(null);
        }
    }

    private void setupCategoryCard() {
        TextView wordCountTv = getActivity().findViewById(R.id.catdet_wordCount);
        nameEt = getActivity().findViewById(R.id.catdet_name);
        descEt = getActivity().findViewById(R.id.catdet_desc);
        CardView cardView = getActivity().findViewById(R.id.catdet_cat_card);

        nameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    nameEt.getBackground().setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSecondary500), PorterDuff.Mode.SRC_IN);
                } else {
                    nameEt.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                }
            }
        });
        nameEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    nameEt.setError("Can't be empty!");
                } else {
                    nameEt.setError(null);
                }
            }

        });

        descEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    descEt.getBackground().setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSecondary500), PorterDuff.Mode.SRC_IN);
                } else {
                    descEt.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                }
            }
        });

        categoryViewModel.getById().observe(this, new Observer<Category>() {
            @Override
            public void onChanged(@Nullable Category category) {
                if (category != null) {
                    wordCountTv.setText(category.getWordCount() + " Word" + (category.getWordCount() == 1 ? "" : "s"));
                    if (!catdet_editMode.getState()) {
                        nameEt.setText(category.getName());
                        nameEt.setHint("Category   ");
                        descEt.setText(category.getDesc());
                    }
                    cardView.setCardBackgroundColor(Color.parseColor(category.getColorHex()));

                    progressLy.setVisibility(View.VISIBLE);
                    circularBack(progressLy, ColorUtils.blendARGB(Color.parseColor(category.getColorHex()),
                            ContextCompat.getColor(getContext(),android.R.color.black),0.2f));
                    if (category.getWordCount() != 0) {
                        ProgressBarAnimation anim = new ProgressBarAnimation(progressBar,progressBar.getProgress(),
                                ((float)category.getLearnedWordCnt() / category.getWordCount())*100);
                        anim.setStartOffset(250);
                        anim.setDuration(500);
                        progressBar.startAnimation(anim);
                    }
                }
            }
        });
    }
    public static void circularBack(View v, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        v.setBackground(shape);
    }

    public class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }

    void setupParentUI() {
        setHasOptionsMenu(true);

        // Set toolbar title
        getActivity().setTitle("Category Detail");

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);

        // Set current tab in navigation view
        NavigationView menu = getActivity().findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.catTab);

        // Implement floating action button functionality
        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Here: -1 means that we are going to be using the fragment for adding a new word */
                startWordDetailFrag(-1);
            }
        });
        if (!fab.isShown()) fab.show();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState!=null){
            catdet_editMode.setState(savedInstanceState.getBoolean("catdet_editMode"));
            catdet_selectedItems.setList(savedInstanceState.getIntegerArrayList("catdet_selectedItems"));
            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("KeyForLayoutManagerState"));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("categoryId", currentId);
        outState.putBoolean("catdet_editMode", catdet_editMode.getState());
        if (catdet_selectedItems!=null) {
            outState.putIntegerArrayList("catdet_selectedItems", new ArrayList<Integer>(catdet_selectedItems.getList()));
        }
        outState.putParcelable("KeyForLayoutManagerState", recyclerView.getLayoutManager().onSaveInstanceState());
    }


    @Override
    public void onDestroy() {
//        wordViewModel.getAllByCategoryId().setValue(null);
        super.onDestroy();
        catdet_editMode.clearListeners();
        if (catdet_editMode.getState())
            catdet_editMode.setState(false);
        catdet_selectedItems.clearListeners();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.catdet_main_bar, menu);

        boolean visible = catdet_editMode.getState();
        menu.findItem(R.id.action_edit2).setVisible(!visible);
        menu.findItem(R.id.action_stats).setVisible(!visible);
        menu.findItem(R.id.action_apply2).setVisible(visible);
        menu.findItem(R.id.action_color_picker).setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit2:
                if (recyclerView.getAdapter()!=null) {
                    if (recyclerView.getAdapter().getItemCount()!=0) {
                        catdet_editMode.setState(true);
                    }
                }
                return true;

            case R.id.action_apply2:
                // Check if fields are filled correctly
                if (nameEt.getError() != null) {
                    Toast.makeText(getActivity(), "Fields are filled incorrectly", Toast.LENGTH_SHORT).show();

                } else {
                    catdet_editMode.setState(false);
                }
                return true;

            case R.id.action_color_picker:
                if (categoryViewModel.getById().getValue() != null && getActivity()!=null && nameEt.getError()==null) {
                    new ColorPickerDialog(
                        getActivity(),
                        Color.parseColor(categoryViewModel.getById().getValue().getColorHex()),
                        "Choose a card color",
                        BckColors.toArr(),
                        new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                if (color!=Color.parseColor(categoryViewModel.getById().getValue().getColorHex())) {
                                    categoryViewModel.getById().getValue().setColorHex(String.format("#%06X", (0xFFFFFF & color)));
                                    categoryViewModel.getById().postValue(categoryViewModel.getById().getValue());
                                    categoryViewModel.update(categoryViewModel.getById().getValue());
                                    updateCategoryCard();
                                    categoryViewModel.getById(currentId);
                                }
                            }
                            }).show();
                }
                return true;

            case R.id.action_stats:
                String tag = getResources().getString(R.string.fragment_statistics_tag);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(tag);
                if (fragment == null) fragment = new StatisticsFragment();

                /* pass the position in a bundle */
                Bundle bundle = new Bundle();
                bundle.putInt("categoryId",currentId);
                fragment.setArguments(bundle);

                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_fade_in, R.anim.slide_fade_out, R.anim.slide_fade_in, R.anim.slide_fade_out)
                        .replace(R.id.main_layout, fragment, tag)
                        .addToBackStack(getResources().getString(R.string.fragment_categories_detail_tag))
                        .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QUIZ_ACTIVITY_RQ) {
            if (resultCode == RESULT_OK) {
                categoryViewModel.getById(currentId);
            }
        }
    }

    // Action Toolbar callback method for: 'edit_bar' i.e 'editActionMode'
    private ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cat_edit_bar,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.action_delete) {
                catdet_selectedItems.delete();
                mode.finish();

            } else if (id == R.id.action_select_all) {
                catdet_selectedItems.selectAll();

            } else {
                return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            catdet_selectedItems.selectNone();
            editActionMode = null;
        }
    };
}
