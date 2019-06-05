package com.sd.coursework.Utils.Adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sd.coursework.Database.Entity.Category;
import com.sd.coursework.R;
import com.sd.coursework.Utils.ListenerBoolWrapper;
import com.sd.coursework.Utils.ListenerIntWrapper;
import com.sd.coursework.Utils.UI.ItemMoveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sd.coursework.CategoryFragment.cat_editMode;
import static com.sd.coursework.CategoryFragment.cat_selectedItems;

public class CategoryAdapter
            extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder>
            implements ItemMoveCallback.ItemTouchHelperContract {

    // Main data model
    private List<Category> categories = new ArrayList<>();

    // Flag to tell adapter to switch to cat_main_bar mode layout (show/hide views)
    private boolean editModeL = cat_editMode.getState();

    // Flag to tell adapter if item order needs to be updated
    private boolean reordered = false;


    // Interfaces
    /* Used to assist in the drag and drop functionality */
    public interface StartDragListener {
        void requestDrag(RecyclerView.ViewHolder viewHolder);
    }
    private final StartDragListener mStartDragListener;

    /** Interface that allows parent fragment to implement necessary functions
        that update the main data model {@link CategoryAdapter#categories}
    */
    public interface ViewModelListener {
        void updateItemOrder(List<Category> liveList);
        void updateSelectedItems(List<Category> liveList);
        void startDetailFragment(int categoryId);
    }
    private final ViewModelListener viewModelListener;

    // Constructors / Setters & Getters
    public CategoryAdapter(StartDragListener mStartDragListener, ViewModelListener vml) {
        this.mStartDragListener = mStartDragListener;
        this.viewModelListener = vml;

        // listener to detect user wanting to go into list "cat_main_bar mode"
        cat_editMode.setListener(new ListenerBoolWrapper.ChangeListener() {
            @Override
            public void onChange(boolean state) {
                editModeL = cat_editMode.getState();

                if(!editModeL && reordered) {   //On cat_main_bar mode exit
                    viewModelListener.updateItemOrder(categories);

                    reordered = false; //Reset to default value for next cat_main_bar session

                } else {                        //On cat_main_bar mode enter
                    for (Category c : categories) { c.setChecked(false); }
                }
                notifyDataSetChanged();
            }
        });

        // listener to select all/none of the items in the list
        cat_selectedItems.setListener(new ListenerIntWrapper.SelectListener() {
            @Override
            public void onSelectAll() {
                for (Category c : categories) { c.setChecked(true); }
                viewModelListener.updateSelectedItems(categories);
                notifyDataSetChanged();
            }

            @Override
            public void onSelectNone() {
                for (Category c : categories) { c.setChecked(false); }
                viewModelListener.updateSelectedItems(categories);
                notifyDataSetChanged();
            }
        });
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView wordCountTv;
        private TextView nameTv;
        private TextView descTv;
        private ImageView dragHandleIv;
        private CheckBox selectCb;

        CategoryHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.wordCountTv = itemView.findViewById(R.id.cat_wordCountTv);
            this.nameTv = itemView.findViewById(R.id.cat_nameTv);
            this.descTv = itemView.findViewById(R.id.cat_descTv);
            this.dragHandleIv = itemView.findViewById(R.id.cat_drag_handleIv);
            this.selectCb = itemView.findViewById(R.id.cat_selectCh);
        }
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categories, parent, false);

        return new CategoryHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int pos) {
        Category currentCat = categories.get(pos);

        holder.wordCountTv.setText(currentCat.getWordCount() + " Word" + (currentCat.getWordCount()==1?"":"s"));
        holder.nameTv.setText(currentCat.getName());
        holder.descTv.setText(currentCat.getDesc());
        holder.itemView.setBackgroundColor(Color.parseColor(currentCat.getColorHex()));

        if(!editModeL) {
            holder.dragHandleIv.setVisibility(View.GONE);
            holder.selectCb.setVisibility(View.GONE);

            //Listeners
            //Transfer over to word fragment
            holder.itemView.setOnClickListener(v -> {
                viewModelListener.startDetailFragment(currentCat.getId());
            });

            // Allow to enter to cat_main_bar mode on long press and hold
            holder.itemView.setOnLongClickListener(v -> {
                cat_editMode.setState(true);
                currentCat.setChecked(true);
                viewModelListener.updateSelectedItems(categories);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            });

        } else {
            holder.dragHandleIv.setVisibility(View.VISIBLE);
            holder.selectCb.setVisibility(View.VISIBLE);

            //Values
            holder.selectCb.setOnCheckedChangeListener(null);
            holder.selectCb.setChecked(currentCat.isChecked());

            //Listeners
            holder.itemView.setOnLongClickListener(null);
            holder.itemView.setOnClickListener(v -> {
                holder.selectCb.setChecked(!holder.selectCb.isChecked());
            });

            holder.selectCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                currentCat.setChecked(isChecked);
                viewModelListener.updateSelectedItems(categories);
            });

            holder.dragHandleIv.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mStartDragListener.requestDrag(holder);
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        reordered = true;
        Collections.swap(categories, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }
}