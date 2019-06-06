package com.sd.coursework.Utils.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.HapticFeedbackConstants;
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
    private int totalCount;

    public interface QuizAdapterListener {
        void notifyFlipped(int position);
        void showDetailedDialog(String word, String def);
    }
    private QuizAdapterListener quizAdapterListener;

    private Context context;
    public QuizAdapter(Context context, int totalCount, QuizAdapterListener qal) {
        this.context = context;
        this.totalCount = totalCount;
        this.quizAdapterListener = qal;
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
                quizAdapterListener.notifyFlipped(position);
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
        TextView cardCountTv = view.findViewById(R.id.quiz_card_counterTv);

        WordLite currentCard = items.get(position);
        wordTv.setText(currentCard.getWordName());
        defiTv.setText(currentCard.getWordDef());
        cardCountTv.setText(position+1 + "/" + totalCount);

        cardEFV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (((EasyFlipView)v).isBackSide()) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    quizAdapterListener.showDetailedDialog(currentCard.getWordName(), currentCard.getWordDef());
                    return true;
                }
                return false;
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
