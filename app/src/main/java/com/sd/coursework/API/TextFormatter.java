package com.sd.coursework.API;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {
    private static ArrayList<Token> tokens = new ArrayList<>();
    private static ArrayList<Token> groupingTokens = new ArrayList<>();

    private class Token {
        String tag;
        String tagNS, tagNE; //Tag new start, tag new end
        Token(String tag, String tagNS, String tagNE) {
            this.tag = tag;
            this.tagNS = tagNS;
            this.tagNE = tagNE;
        }
        Token(String tag, String tagN) {
            this.tag = tag;
            this.tagNS = tagN;
        }
    }

    private void populateTokens() {
        tokens.add(new Token("bc","")); //bold colon and space (useless for my implementation)
        tokens.add(new Token("b","<b>","</b>")); //Bold
        tokens.add(new Token("inf","<sub>","</sub>")); //Subscript
        tokens.add(new Token("sup","<sup>","</sup>")); //Superscript
        tokens.add(new Token("it","<i>","</i>")); //Italic

        tokens.add(new Token("ldquo","&#8220;")); //left & right double quotes
        tokens.add(new Token("rdquo","&#8221;"));
        tokens.add(new Token("gloss","[","]")); //square brackets
        tokens.add(new Token("phrase","<b><i>","</b></i>")); //bold and italic
        tokens.add(new Token("qword","<i>","</i>")); //italic
        tokens.add(new Token("wi","<i>","</i>"));
        tokens.add(new Token("sc","","")); //small capitals (Hard to implement)
        tokens.add(new Token("parahw","","")); //slanted text (useless for my implementation)


        //CROSS-REFERENCE GROUPING TOKENS
        groupingTokens.add(new Token("dx","")); // takes care dxt
        groupingTokens.add(new Token("dx_def",""));
        groupingTokens.add(new Token("dx_ety",""));
        groupingTokens.add(new Token("ma","")); // takes care of mat
        groupingTokens.add(new Token("et_link",""));
        groupingTokens.add(new Token("i_link",""));
    }

    public String parse(String text) {
        if (tokens.isEmpty() || groupingTokens.isEmpty()) populateTokens();

        // Replaces API returned local formatting tokens to HTML ones
        for (Token t : tokens) {
            text = text.replaceAll("\\{"+t.tag+"[^\\}]*\\}",t.tagNS);

            if (t.tagNE!=null) {
                text = text.replaceAll("\\{(\\/"+t.tag+"|\\\\\\/"+t.tag+")[^\\}]*\\}",t.tagNE);
            }
        }

        // Removes non-formatting tokens (tags along with everything in between)
        for (Token t : groupingTokens) {
            text = text.replaceAll("\\{[\\s]*"+t.tag+"[^\\{\\\\]*\\\\\\/"+t.tag+"\\}" +
                                        "|\\{[\\s]*"+t.tag+"[^\\/]*\\/"+t.tag+"\\}",t.tagNS);
        }

        text = purgeTokenTags(text,"a_link");
        text = purgeTokenTags(text,"d_link");
        text = purgeTokenTags(text,"sx");

        return text;
    }

    // Removes non-formatting tokens (but keeps first field of tag)
    private String purgeTokenTags(String text, String token) {
        Pattern p1 = Pattern.compile("\\{[\\s]*"+token+"[^\\}]*\\}");
        Pattern p2 = Pattern.compile("(?<=\\{"+token+"\\|)[^|\\}]*(?=\\||\\})");
        Matcher m1 = p1.matcher(text);
        Matcher m2 = p2.matcher(text);

        int c = 0;
        while (m1.find() && m2.find()) {
            text = text.replace(m1.group(0), m2.group(0));
        }

        return text;
    }
}
