package com.sd.coursework.Database.Entity.TypeConverters;

import android.arch.persistence.room.TypeConverter;

import java.util.ArrayList;

public class ArrayListConverter {
    @TypeConverter
    public ArrayList<Integer> fromWordIds(String value) {
        String[] strArr = value.split(",",0);

        ArrayList<Integer> arr = new ArrayList<>();
        for (String str : strArr) {
            if (!str.isEmpty())
                arr.add(Integer.parseInt(str.trim()));
        }

        return arr;
    }

    @TypeConverter
    public String ToWordIds(ArrayList<Integer> arr) {
        String value = "";

        for (int v :arr)
            value += v + ",";

        return value;
    }
}
