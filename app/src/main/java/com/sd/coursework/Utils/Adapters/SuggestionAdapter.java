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
import com.sd.coursework.Utils.Dialogs.SuggestionDetailDialogFrag;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionHolder> {
    private List<ResultQuery.SummarizedSuggestion> suggestions = new ArrayList<>();
    private Context context;

    public interface ViewModelListener {
        void setDefinition(String word, String def);
    }

    @NonNull
    private ViewModelListener viewModelListener;

    // Constructors / Setters & Getters
    public SuggestionAdapter(Context context, ViewModelListener vml) {
        this.viewModelListener = vml;
        this.context = context;
    }

    public void setWords(List<ResultQuery.SummarizedSuggestion> words) {
        this.suggestions = words;
        notifyDataSetChanged();
    }

    class SuggestionHolder extends RecyclerView.ViewHolder {
        private TextView dictionaryTv;
        private TextView wordTv;
        private TextView posTv;
        private TextView definitionTv;
        private ImageView expandIv;
        private View itemView;

        SuggestionHolder(View itemView) {
            super(itemView);
            this.dictionaryTv = itemView.findViewById(R.id.suggestion_dictionaryTv);
            this.wordTv = itemView.findViewById(R.id.suggestion_wordTv);
            this.posTv = itemView.findViewById(R.id.suggestion_posTv);
            this.definitionTv = itemView.findViewById(R.id.suggestion_definitionTv);
            this.expandIv = itemView.findViewById(R.id.suggestion_expandIv);
            this.itemView = itemView;
        }
    }

    @NonNull
    @Override
    public SuggestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestions, parent, false);

        return new SuggestionHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionHolder holder, int pos) {
        ResultQuery.SummarizedSuggestion currentSuggestion = suggestions.get(pos);

        holder.dictionaryTv.setText(currentSuggestion.getDictName());
        holder.wordTv.setText(currentSuggestion.getWord());
        holder.definitionTv.setText(Html.fromHtml(currentSuggestion.getDefinition()));
        String partOfSpeech = currentSuggestion.getPOS();
        if (partOfSpeech!=null) {
            holder.posTv.setText("(" + partOfSpeech + ")");
        }
        Log.i("SuggestionAdapter",currentSuggestion.getDefinition());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModelListener.setDefinition(currentSuggestion.getWord(), Html.fromHtml(currentSuggestion.getDefinition()).toString());
            }
        });

        holder.expandIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuggestionDetailDialogFrag dialog = new SuggestionDetailDialogFrag();
                dialog.setIdentifier(currentSuggestion.getTracker());
                dialog.show(((AppCompatActivity)context).getSupportFragmentManager(),
                                                    context.getString(R.string.dialog_new_category_tag));
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }
}
