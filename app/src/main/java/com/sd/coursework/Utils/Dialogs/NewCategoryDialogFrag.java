package com.sd.coursework.Utils.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sd.coursework.R;
import com.sd.coursework.Utils.BckColors;

public class NewCategoryDialogFrag extends AppCompatDialogFragment {
    public interface NewCategoryListener {
        void onOkClicked(String category, String description, String colorHex);
    }

    public void setListener(NewCategoryListener listener) {
        this.listener = listener;
    }

    private EditText categoryEt, descriptionEt;
    private Button  colorPickerBt;
    private NewCategoryListener listener;
    private String colorHex = "#FFFFFF";
    private boolean okBtnState = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_category,null);

        if (savedInstanceState!=null) {
            colorHex = savedInstanceState.getString("colorHex");
            okBtnState = savedInstanceState.getBoolean("okBtnState");
        }

        categoryEt = view.findViewById(R.id.ac_categoryEt);
        categoryEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (categoryEt.getText().toString().equals("")) {
                    categoryEt.setError("Can't be empty!");
                    okBtnState = false;
                } else {
                    categoryEt.setError(null);
                    okBtnState = true;
                }
                ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(okBtnState);
            }
        });

        descriptionEt = view.findViewById(R.id.ac_desEt);
        colorPickerBt = view.findViewById(R.id.ac_color_pickerBt);
        colorPickerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        builder.setView(view)
                .setTitle("New Category")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!categoryEt.getText().toString().isEmpty()) {
                            listener.onOkClicked(categoryEt.getText().toString(),
                                    descriptionEt.getText().toString(),
                                    colorHex);
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        // disable positive (ok) button by default
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(okBtnState);
        colorPickerBt.setBackgroundColor(Color.parseColor(colorHex));
    }

    private void showColorPicker() {
        new ColorPickerDialog(
                getActivity(),
                Color.parseColor(colorHex),
                "Choose a card color",
                BckColors.toArr(),
                new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        colorHex = String.format("#%06X", (0xFFFFFF & color));
                        colorPickerBt.setBackgroundColor(Color.parseColor(colorHex));
                    }
                }).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("colorHex",colorHex);
        outState.putBoolean("okBtnState",okBtnState);
        super.onSaveInstanceState(outState);
    }
}

