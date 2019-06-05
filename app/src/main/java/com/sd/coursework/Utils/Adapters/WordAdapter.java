package com.sd.coursework.Utils.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaredrummler.fastscrollrecyclerview.FastScrollRecyclerView;
import com.sd.coursework.Database.Entity.WordPlus;
import com.sd.coursework.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordHolder>
        implements FastScrollRecyclerView.SectionedAdapter, Filterable {

    // Main data model
    private List<WordPlus> words = new ArrayList<>();
    private List<WordPlus> wordsFiltered = new ArrayList<>();

    @NonNull
    @Override
    public String getSectionName(int position) {
        return wordsFiltered.get(position).getName().substring(0, 1).toUpperCase(Locale.ENGLISH);
    }


    public interface WordAdapterListener {
        void startWordDetailedFrag(int catId, int wordId);
    }
    @NonNull
    private WordAdapterListener wordAdapterListener;

    // Constructors / Setters & Getters
    public WordAdapter(WordAdapterListener wal) {
        this.wordAdapterListener = wal;
    }

    public void setWords(List<WordPlus> words) {
        this.words = words;
        this.wordsFiltered = new ArrayList<>(words);
        notifyDataSetChanged();
    }

    class WordHolder extends RecyclerView.ViewHolder {
        private TextView categoryTv;
        private TextView nameTv;
        private TextView defiTv;
        private ImageView dragHandleIv;
        private CheckBox selectCb;

        WordHolder(View itemView) {
            super(itemView);
            this.categoryTv = itemView.findViewById(R.id.word_categoryTv);
            this.nameTv = itemView.findViewById(R.id.word_nameTv);
            this.defiTv = itemView.findViewById(R.id.word_defiTv);
            this.dragHandleIv = itemView.findViewById(R.id.word_drag_handleIv);
            this.selectCb = itemView.findViewById(R.id.word_selectCh);
        }
    }

    @NonNull
    @Override
    public WordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_words, parent, false);

        return new WordHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WordHolder holder, int pos) {
        WordPlus currentWord = wordsFiltered.get(pos);

        holder.nameTv.setText(currentWord.getName());
        holder.defiTv.setText(currentWord.getDefinition());
        holder.categoryTv.setText(currentWord.getCategoryName());
        holder.dragHandleIv.setVisibility(View.GONE);
        holder.selectCb.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordAdapterListener.startWordDetailedFrag(
                        currentWord.getCategoryId(), currentWord.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordsFiltered.size();
    }



    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<WordPlus> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(words);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (WordPlus item : words) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            wordsFiltered.clear();
            wordsFiltered.addAll((List<WordPlus>)results.values);
            notifyDataSetChanged();
        }
    };

}