package com.sd.coursework.Utils.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sd.coursework.API.ResultQuery;
import com.sd.coursework.API.Suggestion;
import com.sd.coursework.API.TextFormatter;
import com.sd.coursework.R;

import java.util.ArrayList;

public class SuggestionDetailDialogFrag extends AppCompatDialogFragment {
    private int[] id;
    private LinearLayout.LayoutParams layoutParams;

    public void setIdentifier(int[] tracker) {
        this.id = tracker;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (savedInstanceState!=null) {
            id = savedInstanceState.getIntArray("id");
        }

        // gather all needed suggestion data from query result, use tracker(id) to get specific suggestion
        ArrayList<Suggestion> words = ResultQuery.getSuggestions();

        Suggestion suggestion = words.get(id[0]);
        Suggestion.Meta meta = suggestion.getDefinitions().get(id[1]);
        Suggestion.Sense sense = meta.getSenses().get(id[2]);

        String fl = sense.getVerbDivider();
        if (fl == null) fl = meta.getFunctionalLabel();

        ArrayList<Suggestion.Def> sdSenses = sense.getSdSenses();

        /* Generate layout programmatically based on contents */
        // init constants and views
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout mainView = new LinearLayout(getContext());
        mainView.setOrientation(LinearLayout.VERTICAL);
        mainView.setLayoutParams(layoutParams);

        // Word
        Spanned s;
        if (meta.getWordNumber()!=null) {
            s = Html.fromHtml(meta.getWordName()+" <strong>("+meta.getWordNumber()+")</strong>");
        } else {
            s = Html.fromHtml(meta.getWordName());
        }

        // Word Name
        TextView wordTv = new TextView(getContext());
        wordTv.setTypeface(ResourcesCompat.getFont(getContext(),R.font.varela_round));
        wordTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
        wordTv.setText(s);
        mainView.addView(wordTv);

        // Part of speech
        TextView flTv = createNewTv(25,true);
        flTv.setText(fl);
        mainView.addView(flTv);

        // Definition
        mainView.addView(getDefView(sense.getDef()));

        // Similar close definitions & usage examples
        if (sdSenses.size()!=0) {
            TextView alsoTv = createNewTv(0,false);
            alsoTv.setText("also:");
            mainView.addView(alsoTv);

            for (Suggestion.Def sdSense : sdSenses) {
                mainView.addView(getDefView(sdSense));
            }
        }

        // Dictionary Name
        TextView dictNameTv = new TextView(getContext());
        dictNameTv.setTypeface(ResourcesCompat.getFont(getContext(),R.font.roboto_condensed_regular));
        dictNameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        dictNameTv.setText("\nProvided by: " + suggestion.getDictionary());

        mainView.addView(dictNameTv);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_suggestion_detail,null);

        ViewGroup parent = (ViewGroup) view.findViewById(R.id.scrollArea);
        parent.addView(mainView, 0, layoutParams);

        builder.setView(view)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    private TextView createNewTv(int marginBottom, boolean setBlackColor) {
        TextView returnTv = new TextView(getContext());
        returnTv.setLayoutParams(getLayoutParamWithMargins(marginBottom));
        returnTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        returnTv.setTypeface(ResourcesCompat.getFont(getContext(),R.font.noto_sans));
        if (setBlackColor) {
            returnTv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }
        return returnTv;
    }

    private LinearLayout.LayoutParams getLayoutParamWithMargins(int marginBottom) {
        layoutParams.setMargins(0,0,0, marginBottom);
        return layoutParams;
    }

    private LinearLayout getDefView(Suggestion.Def def) {
        LinearLayout returnLy = new LinearLayout(getContext());
        returnLy.setOrientation(LinearLayout.VERTICAL);
        returnLy.setLayoutParams(getLayoutParamWithMargins(25));

        TextView defTv = createNewTv(25,true);
        defTv.setText(Html.fromHtml(new TextFormatter().parse(def.getDt())));
        returnLy.addView(defTv);

        if (def.getVis().size()!=0){
            TextView exampleTv = createNewTv(0,false);
            exampleTv.setText("usage ex:");
            returnLy.addView(exampleTv);

            for (String vies : def.getVis()) {
                TextView newVTv = createNewTv(15,true);
                newVTv.setText(Html.fromHtml(new TextFormatter().parse(vies)));
                returnLy.addView(newVTv);
            }
        }

        return returnLy;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putIntArray("id",id);
        super.onSaveInstanceState(outState);
    }
}

