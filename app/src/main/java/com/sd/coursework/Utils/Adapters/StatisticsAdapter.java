package com.sd.coursework.Utils.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticHolder> {
    private List<Result> results = new ArrayList<>();

    public interface StatisticsAdapterListener {
        void startStatisticDetailFrag(int pos);
    }

    @NonNull
    private StatisticsAdapterListener statisticListener;
    public StatisticsAdapter(StatisticsAdapterListener sal) {
        this.statisticListener = sal;
    }

    public void setResults(List<Result> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    class StatisticHolder extends RecyclerView.ViewHolder {
        private TextView displayNumTv;
        private TextView dateTv;
        private TextView scoreTv;
        private View itemView;

        StatisticHolder(View itemView) {
            super(itemView);
            this.displayNumTv = itemView.findViewById(R.id.statistic_display_numTv);
            this.dateTv = itemView.findViewById(R.id.statistic_dateTv);
            this.scoreTv = itemView.findViewById(R.id.statistic_scoreTv);
            this.itemView = itemView;
        }
    }

    @NonNull
    @Override
    public StatisticHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statistics, parent, false);

        return new StatisticHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticHolder holder, int pos) {
        Result currentResult = results.get(pos);

        holder.displayNumTv.setText("Quiz #" + (getItemCount()-pos));

        SimpleDateFormat dateFormat =  new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.getDefault());
        holder.dateTv.setText(dateFormat.format(currentResult.getTakenTS()));

        int totalWords = (currentResult.getCorrectWordCnt() + currentResult.getWrongWordIds().size());
        holder.scoreTv.setText(currentResult.getCorrectWordCnt() + "/" + totalWords) ;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statisticListener.startStatisticDetailFrag(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}

