package com.sd.coursework.Utils.Adapters;

import android.annotation.SuppressLint;
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

import com.sd.coursework.Database.Entity.Word;
import com.sd.coursework.R;
import com.sd.coursework.Utils.ListenerBoolWrapper;
import com.sd.coursework.Utils.ListenerIntWrapper;
import com.sd.coursework.Utils.UI.ItemMoveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sd.coursework.CategoryDetailFragment.catdet_editMode;
import static com.sd.coursework.CategoryDetailFragment.catdet_selectedItems;

/** NOTICE: for properly annotated adapter, check {@link CategoryAdapter},
 *  as this adapter is conceptually similar
 *  */
public class CategoryDetailAdapter
            extends RecyclerView.Adapter<CategoryDetailAdapter.CategoryDetailHolder>
            implements ItemMoveCallback.ItemTouchHelperContract {

    private List<Word> words = new ArrayList<>();
    private boolean editModeL = catdet_editMode.getState();
    private boolean reordered = false;

    public interface StartDragListener {
        void requestDrag(RecyclerView.ViewHolder viewHolder);
    }
    private final StartDragListener mStartDragListener;

    public interface CategoryDetailListener {
        void updateItemOrder(List<Word> liveList);
        void updateSelectedItems(List<Word> liveList);
        void startWordDetailFragment(int id);
    }
    private final CategoryDetailListener categoryDetailListener;

    // Constructors / Setters & Getters
    public CategoryDetailAdapter(StartDragListener mStartDragListener, CategoryDetailListener cdl) {
        this.mStartDragListener = mStartDragListener;
        this.categoryDetailListener = cdl;

        catdet_editMode.setListener(new ListenerBoolWrapper.ChangeListener() {
            @Override
            public void onChange(boolean state) {
                editModeL = catdet_editMode.getState();

                if(!editModeL && reordered) {
                    categoryDetailListener.updateItemOrder(words);

                    reordered = false;

                } else {
                    for (Word w : words) { w.setChecked(false); }
                }
                notifyDataSetChanged();
            }
        });

        catdet_selectedItems.setListener(new ListenerIntWrapper.SelectListener() {
            @Override
            public void onSelectAll() {
                for (Word w : words) { w.setChecked(true); }
                categoryDetailListener.updateSelectedItems(words);
                notifyDataSetChanged();
            }

            @Override
            public void onSelectNone() {
                for (Word w : words) { w.setChecked(false); }
                categoryDetailListener.updateSelectedItems(words);
                notifyDataSetChanged();
            }
        });
    }

    public void setWords(List<Word> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    class CategoryDetailHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView categoryTv;
        private TextView nameTv;
        private TextView defiTv;
        private ImageView dragHandleIv;
        private CheckBox selectCb;

        CategoryDetailHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.categoryTv = itemView.findViewById(R.id.word_categoryTv);
            this.nameTv = itemView.findViewById(R.id.word_nameTv);
            this.defiTv = itemView.findViewById(R.id.word_defiTv);
            this.dragHandleIv = itemView.findViewById(R.id.word_drag_handleIv);
            this.selectCb = itemView.findViewById(R.id.word_selectCh);
        }
    }

    @NonNull
    @Override
    public CategoryDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_words, parent, false);

        return new CategoryDetailHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull CategoryDetailHolder holder, int pos) {
        Word currentWord = words.get(pos);

        holder.nameTv.setText(currentWord.getName());
        holder.defiTv.setText(currentWord.getDefinition());
        holder.categoryTv.setVisibility(View.GONE);

        if(!editModeL) {
            holder.dragHandleIv.setVisibility(View.GONE);
            holder.selectCb.setVisibility(View.GONE);

            //Listeners
            //Transfer over to word fragment
            holder.itemView.setOnClickListener(v -> {
                categoryDetailListener.startWordDetailFragment(currentWord.getId());
            });

            // Allow to enter to catdet_main_bar mode on long press and hold
            holder.itemView.setOnLongClickListener(v -> {
                catdet_editMode.setState(true);
                currentWord.setChecked(true);
                categoryDetailListener.updateSelectedItems(words);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            });

        } else {
            holder.dragHandleIv.setVisibility(View.VISIBLE);
            holder.selectCb.setVisibility(View.VISIBLE);

            //Values
            holder.selectCb.setOnCheckedChangeListener(null);

            if (catdet_selectedItems.getList().contains(currentWord.getId())) {
                holder.selectCb.setChecked(true);
            } else {
                holder.selectCb.setChecked(false);
            }
            holder.selectCb.setChecked(currentWord.isChecked());

            //Listeners
            holder.itemView.setOnLongClickListener(null);
            holder.itemView.setOnClickListener(v -> {
                holder.selectCb.setChecked(!holder.selectCb.isChecked());
            });

            holder.selectCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                currentWord.setChecked(isChecked);
                categoryDetailListener.updateSelectedItems(words);
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
        return words.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        reordered = true;
        Collections.swap(words, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }
}
