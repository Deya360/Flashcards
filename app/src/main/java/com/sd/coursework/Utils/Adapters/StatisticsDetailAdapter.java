package com.sd.coursework.Utils.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sd.coursework.Database.Entity.ResultLite;
import com.sd.coursework.R;

import java.util.ArrayList;
import java.util.List;

public class StatisticsDetailAdapter extends RecyclerView.Adapter<StatisticsDetailAdapter.StatisticDetailHolder> {
    private List<ResultLite> results = new ArrayList<>();

    public interface StatisticsDetailAdapterListener {
        void startWordDetailedFrag(int wordId);
    }

    @NonNull
    private StatisticsDetailAdapterListener statisticsDetailAdapterListener;
    public StatisticsDetailAdapter(StatisticsDetailAdapterListener sdal) {
        this.statisticsDetailAdapterListener = sdal;
    }

    public void setResults(List<ResultLite> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    class StatisticDetailHolder extends RecyclerView.ViewHolder {
        private TextView wordTv;
        private ImageView resIv;

        StatisticDetailHolder(View itemView) {
            super(itemView);
            this.wordTv = itemView.findViewById(R.id.statwords_wordTv);
            this.resIv = itemView.findViewById(R.id.statwords_resIv);
        }
    }

    @NonNull
    @Override
    public StatisticDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statistics_detail, parent, false);

        return new StatisticDetailHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticDetailHolder holder, int pos) {
        ResultLite currentResult = results.get(pos);

        holder.wordTv.setText(currentResult.getWord());

        int imageRec;
        if (currentResult.isCorrect()) imageRec = R.drawable.ic_correct_24dp;
        else imageRec = R.drawable.ic_wrong_24dp;

        holder.resIv.setImageResource(imageRec);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statisticsDetailAdapterListener.startWordDetailedFrag(currentResult.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}

