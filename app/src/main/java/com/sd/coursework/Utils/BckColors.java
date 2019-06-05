package com.sd.coursework.Utils;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.LinkedList;

//Light material background colors for cards
public enum BckColors {
    White("FFFFFF"),
    Green("C8E6C9"),
    Light_Green("DCEDC8"),
    Lime("F0F4C3"),
    Yellow("FFF9C4"),
    Amber("FFECB3"),
    Orange("FFE0B2"),
    Deep_Orange("FFCCBC"),
    Brown("D7CCC8"),
    Blue_Grey("CFD8DC"),
    Teal("B2DFDB"),
    Cyan("B2EBF2"),
    Light_Blue("B3E5FC"),
    Blue("BBDEFB"),
    Indigo("C5CAE9"),
    Deep_Purple("D1C4E9"),
    Purple("E1BEE7"),
    Pink("F8BBD0"),
    Red("FFCDD2");


    private final String value;
    BckColors(final String value) {
        this.value = value;
    }

    public int toInt() {
        return Color.parseColor("#" + value);
    }
    public static int[] toArr() {
        int[] returnArr = new int[BckColors.values().length];

        int index = 0;
        for (BckColors c : values()) {
            returnArr[index++] = c.toInt();
        }

        return returnArr;
    }

}
