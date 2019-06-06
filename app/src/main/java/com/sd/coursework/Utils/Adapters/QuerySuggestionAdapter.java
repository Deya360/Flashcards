package com.sd.coursework.Utils.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sd.coursework.API.ResultQuery;
import com.sd.coursework.R;

import java.util.ArrayList;
import java.util.List;

/** how is this different from the {@link SuggestionAdapter}??
 *  when displaying 'definition' suggestions for a word, the SuggestionAdapter is used,
 *  however, when the word is misspelled, then we provide 'word' suggestions, for that we
 *  use this adapter */
public class QuerySuggestionAdapter extends RecyclerView.Adapter<QuerySuggestionAdapter.SuggestionSuggestionHolder> {
    private List<String> words = new ArrayList<>();

    public interface QuerySuggestionAdapterListner {
        void setWord(String word);
    }
    @NonNull
    private QuerySuggestionAdapterListner querySuggestionAdapterListner;

    public QuerySuggestionAdapter(QuerySuggestionAdapterListner qsal) {
        this.querySuggestionAdapterListner = qsal;
    }

    public void setWords(List<String> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    class SuggestionSuggestionHolder extends RecyclerView.ViewHolder {
        private TextView wordTv;
        private View itemView;

        SuggestionSuggestionHolder(View itemView) {
            super(itemView);
            this.wordTv = itemView.findViewById(R.id.query_suggestion_wordTv);
            this.itemView = itemView;
        }
    }

    @NonNull
    @Override
    public SuggestionSuggestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_query_suggestions, parent, false);

        return new SuggestionSuggestionHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionSuggestionHolder holder, int pos) {
        holder.wordTv.setText(words.get(pos));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySuggestionAdapterListner.setWord(words.get(pos));
            }
        });

    }

    @Override
    public int getItemCount() {
        return words.size();
    }
}
