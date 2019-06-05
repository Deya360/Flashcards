package com.sd.coursework.Utils;

public class QuizItem {
    int id;
    int outcome;

    public QuizItem(int id, int outcome) {
        this.id = id;
        this.outcome = outcome;
    }

    public int getId() {
        return id;
    }
    public int getOutcome() {
        return outcome;
    }
}