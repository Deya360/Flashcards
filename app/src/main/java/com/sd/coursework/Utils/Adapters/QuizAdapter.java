package com.sd.coursework.Utils.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sd.coursework.Database.Entity.WordLite;
import com.sd.coursework.R;
import com.sd.coursework.Utils.ListenerIntWrapper;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;
import java.util.List;

import static com.sd.coursework.QuizActivity.quiz_FlipCard;

public class QuizAdapter extends PagerAdapter {

    private List<WordLite> items = new ArrayList<>();


    public interface AdapterListener {
        void notifyFlipped(int position);
    }

    private AdapterListener adapterListener;
    private Context context;

    public QuizAdapter(Context context, AdapterListener adapterListener) {
        this.context = context;
        this.adapterListener = adapterListener;
    }

    public void setItems(List<WordLite> items) {
        this.items = new ArrayList<>(items);
        notifyDataSetChanged();
    }


    public void addItem(WordLite item) {
        this.items.add(item);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view==o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz, container, false);

        EasyFlipView cardEFV = view.findViewById(R.id.quiz_card);
        cardEFV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EasyFlipView)v).flipTheView();
                adapterListener.notifyFlipped(position);
            }
        });

        quiz_FlipCard.setListener(new ListenerIntWrapper.ChangeListener() {
            @Override
            public void onChange(int selected_count) {
                if (selected_count==position) {
                    cardEFV.flipTheView();
                }
            }
        });

        TextView wordTv = view.findViewById(R.id.quiz_wordTv);
        TextView defiTv = view.findViewById(R.id.quiz_definitionTv);

        WordLite card = items.get(position);
        wordTv.setText(card.getWordName());
        defiTv.setText(card.getWordDef());

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
