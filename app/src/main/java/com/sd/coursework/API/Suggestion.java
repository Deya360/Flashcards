package com.sd.coursework.API;

import java.util.ArrayList;

public class Suggestion {
    public interface OnTaskCompleted {
        void onTaskCompleted(Boolean success);
    }

    public class Def {
        String dt;
        ArrayList<String> vis = new ArrayList<>();

        public String getDt() {
            return dt;
        }

        public ArrayList<String> getVis() {
            return vis;
        }
    }

    public class Sense {
        String verbDivider; //vd
        Def def; //actual meaning
        ArrayList<Def> sdSenses = new ArrayList<>(); // also

        public String getVerbDivider() {
            return verbDivider;
        }
        public Def getDef() {
            return def;
        }
        public ArrayList<Def> getSdSenses() {
            return sdSenses;
        }
    }

    public class Meta {

        String functionalLabel; //fl
        String wordName;    //meta: id: string(0) (when split by :)
        String wordNumber;    //meta: id: string(1) (when split by :), can be empty

        ArrayList<Sense> senses = new ArrayList<>();    //under def:
        public void addSense(Sense sense) {
            this.senses.add(sense);
        }
        public ArrayList<Sense> getSenses() {
            return senses;
        }

        public String getWordName() {
            return wordName;
        }
        public String getFunctionalLabel() {
            return functionalLabel;
        }

        public String getWordNumber() {
            return wordNumber;
        }
    }

    private String dictionary; //Constant
    private ArrayList<Meta> definitions = new ArrayList<>(); //Sense

    public Suggestion(String dictionary) {
        this.dictionary = dictionary;
    }

    public String getDictionary() {
        return dictionary;
    }
    public ArrayList<Meta> getDefinitions() {
        return definitions;
    }
    public void addDefinition(Meta meta) {
        this.definitions.add(meta);
    }
}
