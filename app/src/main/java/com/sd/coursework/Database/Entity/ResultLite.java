package com.sd.coursework.Database.Entity;

public class ResultLite {
    private int id;
    private String word;
    private boolean isCorrect;

    public ResultLite(int id, String word, boolean isCorrect) {
        this.id = id;
        this.word = word;
        this.isCorrect = isCorrect;
    }

    public int getId() {
        return id;
    }
    public String getWord() {
        return word;
    }
    public boolean isCorrect() {
        return isCorrect;
    }
}

