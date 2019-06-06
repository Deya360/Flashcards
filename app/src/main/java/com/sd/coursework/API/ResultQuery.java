package com.sd.coursework.API;

import java.util.ArrayList;

public class ResultQuery {
    private static ArrayList<Suggestion> suggestions = new ArrayList<>();
    private static ArrayList<String> querySuggestions = new ArrayList<>();
    private static ArrayList<ChangeListener> listeners = new ArrayList<>();

    private static boolean isWordASuggestion = false;
    private static String query = "";

    public static String getQuery() {
        return query;
    }
    public static void setQuery(String query) {
        ResultQuery.query = query;
    }

    public static ArrayList<Suggestion> getSuggestions() {
        return suggestions;
    }
    public static void addSuggestion(Suggestion suggestion) {
        suggestions.add(suggestion);
    }


    public static boolean isIsWordASuggestion() {
        return isWordASuggestion;
    }
    public static void setIsWordASuggestion(boolean isWordASuggestion) {
        if (ResultQuery.isIsWordASuggestion()!=isWordASuggestion) {
            ResultQuery.isWordASuggestion = isWordASuggestion;
            if (!isWordASuggestion) querySuggestions.clear();
        }
    }

    public static ArrayList<String> getQuerySuggestions() {
        return querySuggestions;
    }
    public static void addQuerySuggestions(String querySuggestion) {
        ResultQuery.querySuggestions.add(querySuggestion);
    }

    public static void clear() {
        suggestions.clear();
        querySuggestions.clear();
    }

    public static void postUpdate() {
        if (listeners.size()!=0) {
            if (!isIsWordASuggestion()) {
                ArrayList<SummarizedSuggestion> ss = getSummarizedSuggestions();

                for (ChangeListener l : listeners) {
                    if (l != null) l.onChange(ss);
                }

            } else {
                for (ChangeListener l : listeners) {
                    if (l != null) l.onQueryChange(querySuggestions);
                }
            }

        }
    }

    public static class SummarizedSuggestion {
        private int[] tracker; //technical variable
        private String dictName;
        private String word;
        private String pos; //Part of Speech
        private String definition;

        public SummarizedSuggestion(String dictName, String word, String pos, String definition, int[] tracker) {
            this.dictName = dictName;
            this.word = word;
            this.pos = pos;
            this.definition = definition;
            this.tracker = tracker;
        }

        public String getDictName() {
            return dictName;
        }
        public String getWord() {
            return word;
        }
        public String getPOS() {
            return pos;
        }
        public String getDefinition() {
            return definition;
        }
        public int[] getTracker() {
            return tracker;
        }
    }

    public static ArrayList<SummarizedSuggestion> getSummarizedSuggestions() {
        ArrayList<SummarizedSuggestion> summarizedSuggestions = new ArrayList<>();

        for (int i = 0; i < suggestions.size(); i++) {
            Suggestion sw = suggestions.get(i);
            String dict = sw.getDictionary();

            ArrayList<Suggestion.Meta> definitions = sw.getDefinitions();
            for (int j = 0; j < definitions.size(); j++) {
                Suggestion.Meta meta = definitions.get(j);
                String name = meta.getWordName();

                ArrayList<Suggestion.Sense> senses = meta.getSenses();
                for (int k = 0; k < senses.size(); k++) {
                    Suggestion.Sense sense = senses.get(k);
                    String fl = sense.getVerbDivider();
                    if (fl == null) fl = meta.getFunctionalLabel();


                    String def = sense.getDef().getDt();
                    summarizedSuggestions.add(new SummarizedSuggestion(dict, name, fl, def, new int[]{i, j, k}));
                }
            }
        }

        return summarizedSuggestions;
    }


    public static void setListener(ChangeListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public static void clearListeners() {
        listeners.clear();
    }

    public interface ChangeListener {
        void onChange(ArrayList<SummarizedSuggestion> suggestions);
        void onQueryChange(ArrayList<String> querySuggestions);
    }
}
