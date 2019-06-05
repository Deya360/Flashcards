package com.sd.coursework.Utils.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sd.coursework.Database.Entity.Result;
import com.sd.coursework.R;

import java.util.ArrayList;
import java.util.List;

public class StatisticsDetailAdapter extends RecyclerView.Adapter<StatisticsDetailAdapter.StatisticDetailHolder> {
    private List<Result> results = new ArrayList<>();

    public interface StatisticsDetailAdapterListener {
        void startQuizActivity(int categoryId);
        void startWordDetailedFrag(int catId, int wordId);
    }

    @NonNull
    private StatisticsDetailAdapterListener statisticsDetailAdapterListener;
    public StatisticsDetailAdapter(StatisticsDetailAdapterListener sdal) {
        this.statisticsDetailAdapterListener = sdal;
    }

    public void setResults(List<Result> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    class StatisticDetailHolder extends RecyclerView.ViewHolder {
//        private TextView displayNumTv;

        private View itemView;

        StatisticDetailHolder(View itemView) {
            super(itemView);
//            this.displayNumTv = itemView.findViewById(R.id.statistic_display_numTv);

            this.itemView = itemView;
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
        Result currentResult = results.get(pos);

//        holder.displayNumTv.setText("Quiz #" + (getItemCount()-pos));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                statisticsDetailAdapterListener.startWordDetailedFrag(currentResult.getCategoryId(),sta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}

