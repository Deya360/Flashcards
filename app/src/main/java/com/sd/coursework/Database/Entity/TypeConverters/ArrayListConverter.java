package com.sd.coursework.Database.Entity.TypeConverters;

import android.arch.persistence.room.TypeConverter;

import java.util.ArrayList;

public class ArrayListConverter {
    @TypeConverter
    public ArrayList<String> fromWordIds(String value) {
        String[] strArr = value.split(",",0);

        ArrayList<String> arr = new ArrayList<>();
        for (String str : strArr) {
            if (!str.isEmpty())
                arr.add(str.trim());
        }

        return arr;
    }

    @TypeConverter
    public String ToWordIds(ArrayList<String> arr) {
        StringBuilder value = new StringBuilder();

        for (String v :arr)
            value.append(v).append(",");

        return value.toString();
    }
}
