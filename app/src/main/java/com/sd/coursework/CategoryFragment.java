package com.sd.coursework;

import android.app.Activity;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.Database.VM.CategoryViewModel;
import com.sd.coursework.Utils.Adapters.CategoryAdapter;
import com.sd.coursework.Utils.Dialogs.NewCategoryDialogFrag;
import com.sd.coursework.Utils.ListenerBoolWrapper;
import com.sd.coursework.Utils.ListenerIntWrapper;
import com.sd.coursework.Utils.UI.EmptySupportedRecyclerView;
import com.sd.coursework.Utils.UI.ItemMoveCallback;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment{
    public static ListenerBoolWrapper cat_editMode;
    public static ListenerIntWrapper cat_selectedItems;
    CategoryViewModel categoryViewModel;

    private ItemTouchHelper touchHelper;
    private ActionMode editActionMode;

    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();

        cat_editMode = new ListenerBoolWrapper();
        cat_selectedItems = new ListenerIntWrapper();

        // Initial setup of recycler view
        EmptySupportedRecyclerView recyclerView = getActivity().findViewById(R.id.cat_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);


        /*  - bind adapter
            - implement methods that are responsible for:
                touch helper: for drag and drop assist
                saving item order to local database
                keeping track of selected items
        */
        CategoryAdapter adapter = new CategoryAdapter(new CategoryAdapter.StartDragListener() {
            @Override
            public void requestDrag(RecyclerView.ViewHolder viewHolder) {
                touchHelper.startDrag(viewHolder);
            }
        }, new CategoryAdapter.ViewModelListener() {
            @Override
            public void updateItemOrder(List<Category> liveList) {
                SparseIntArray changeMap = new SparseIntArray();

                //update list positions of items in database
                for (int i = 0; i < liveList.size(); i++) {
                    //update only if changed
                    if (liveList.get(i).getListPosition() != i + 1) {
                        changeMap.put(liveList.get(i).getId(), i + 1);
                    }
                }
                if (changeMap.size() != 0)
                    categoryViewModel.updateListPositions(changeMap);
            }

            @Override
            public void updateSelectedItems(List<Category> liveList) {
                List<Integer> list = new ArrayList<>();

                for (int i = 0; i < liveList.size(); i++) {
                    if (liveList.get(i).isChecked()) {
                        list.add(liveList.get(i).getId());
                    }
                }
                cat_selectedItems.setList(list);
                cat_selectedItems.setMaxSize(liveList.size());
            }

            @Override
            public void startDetailFragment(int categoryId) {
                String tag = getResources().getString(R.string.fragment_categories_detail_tag);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(tag);
                if (fragment == null) fragment = new CategoryDetailFragment();

                /* pass the position in a bundle */
                Bundle bundle=new Bundle();
                bundle.putInt("categoryId", categoryId);
                fragment.setArguments(bundle);

                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_fade_in, R.anim.slide_fade_out, R.anim.slide_fade_in, R.anim.slide_fade_out)
                        .replace(R.id.main_layout, fragment, tag)
                        .addToBackStack(getResources().getString(R.string.fragment_categories_tag))
                        .commit();

                Toast.makeText(getContext(), String.valueOf(categoryId), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Set Empty view, in case the recycler view is empty
        ProgressBar emptyView = (ProgressBar)getActivity().findViewById(R.id.cat_empty_progress_view);
        recyclerView.setPlaceHolderView(emptyView);

        // setup item drag and drop handlers/helpers
        ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // bind data view model
        categoryViewModel = ViewModelProviders.of(getActivity()).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if (categories != null) {
                    adapter.setCategories(categories);
                    if (categories.size()==0) {
                        TextView emptyView = (TextView)getActivity().findViewById(R.id.cat_empty_view);
                        recyclerView.setEmptyView(emptyView);
                    }
                }
            }
        });


        //Initialize static variables and Setup their listeners :

        /*  Listens for edit mode change:
             - Forces toolbar to redraw to include new actions on editMode state change;
             - hides FAB during cat_main_bar mode
        */
        cat_editMode.setListener(new ListenerBoolWrapper.ChangeListener() {
            @Override
            public void onChange(boolean state) {
                if (state) {
                    getActivity().setTitle("Categories: Edit");
                    fab.hide();
                } else {
                    getActivity().setTitle("Categories");
                    fab.show();
                }
                getActivity().invalidateOptionsMenu();
            }
        });

        /*  Listens for selection changes:
                - starts action Mode toolbar when at least one item is selected
                - updates action mode title when number of selected items is changed
                - exits action mode when no items are selected
        */
        cat_selectedItems.setListener(new ListenerIntWrapper.ChangeListener() {
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
        cat_selectedItems.setListener(new ListenerIntWrapper.DeleteListener() {
            @Override
            public void onDelete(List<Integer> itemsList) {
                categoryViewModel.deleteDiscrete(itemsList);
                cat_editMode.setState(false);
                Toast.makeText(view.getContext(), itemsList.size() + " Item" +((itemsList.size()==1)? "":"s")+ " deleted.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /* Save and restore essential variables when activity is destroyed
     *   (screen rotation, keyboard show, screen size change, etc),
     *    so that application fluidity and continuity is preserved
     * */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState!=null){
            cat_editMode.setState(savedInstanceState.getBoolean("cat_editMode"));
            cat_selectedItems.setList(savedInstanceState.getIntegerArrayList("cat_selectedItems"));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("cat_editMode", cat_editMode.getState());
        if (cat_selectedItems!=null) {
            outState.putIntegerArrayList("cat_selectedItems", new ArrayList<Integer>(cat_selectedItems.getList()));
        }
    }


    /*Parent Activity Related UI Changes*/
    void setupUI() {

        /** allows to override {@link CategoryFragment#onCreateOptionsMenu} & {@link CategoryFragment#onOptionsItemSelected} */
        setHasOptionsMenu(true);

        // Set toolbar title
        getActivity().setTitle("Categories");

        // Set current tab in navigation view
        NavigationView menu = getActivity().findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.catTab);

        // Implement floating action button functionality
        fab = getActivity().findViewById(R.id.fab);

        NewCategoryDialogFrag.NewCategoryListener dialogListener = new NewCategoryDialogFrag.NewCategoryListener() {
            @Override
            public void onOkClicked(String category, String description, String colorHex) {
                Log.d("CategoryFragment", "Received");
                int size = categoryViewModel.getAllCategories().getValue().size();
                categoryViewModel.insert(new Category(size, category, description, colorHex,0));
                Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
            }
        };
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewCategoryDialogFrag addCategoryDialog = new NewCategoryDialogFrag();
                addCategoryDialog.setListener(dialogListener);
                addCategoryDialog.show(getFragmentManager(), getString(R.string.dialog_new_category_tag));
            }
        });

        // re-set the listener, (in case screen was rotated, (listeners can't be saved into instance state and are lost on screen rotate))
        Fragment frag = getFragmentManager().findFragmentByTag(getString(R.string.dialog_new_category_tag));
        if (frag!=null) {
            ((NewCategoryDialogFrag)frag).setListener(dialogListener);
        }

        if (!fab.isShown()) fab.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cat_editMode.clearListeners();
        cat_editMode.setState(false);
        cat_selectedItems.clearListeners();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.cat_main_bar, menu);

        menu.findItem(R.id.action_edit).setVisible(!cat_editMode.getState());
        menu.findItem(R.id.action_apply).setVisible(cat_editMode.getState());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                cat_editMode.setState(true);
                return true;

            case R.id.action_apply:
                cat_editMode.setState(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                cat_selectedItems.delete();
                mode.finish();

            } else if (id == R.id.action_select_all) {
                cat_selectedItems.selectAll();

            } else {

                return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            cat_selectedItems.selectNone();
            editActionMode = null;
        }
    };
}
