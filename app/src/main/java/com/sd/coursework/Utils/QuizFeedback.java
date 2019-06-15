package com.sd.coursework.Utils;

import java.util.ArrayList;
import java.util.Random;

//This class contains a collection of some funny/mean snarks, to be shown depending on quiz result
public class QuizFeedback {
    private static ArrayList<String> snarks = new ArrayList<>();

    private void populateSnarks() {
        snarks.add("Next time, try it without the blindfold.");
        snarks.add("Giving up is the only sure way to fail, so try again!");
        snarks.add("If it's any consolation, you did better than we expected.");
        snarks.add("Next time, try it with your eyes open.");

        snarks.add("I hope no one you know saw that score.");
        snarks.add("At least you have a ton of room for improvement.");
        snarks.add("Setting the bar low, huh?");
        snarks.add("Next time, maybe you should phone a friend.");

        snarks.add("Are you going to blame your score on the cat?");
        snarks.add("Trying again is highly encouraged!");
        snarks.add("Buck up, little trooper. You can do better next time.");
        snarks.add("Maybe this will build character.");
        snarks.add("On the bright side, you can't do much worse!");

        snarks.add("Close, but no cigar. And actually it wasn't that close.");
        snarks.add("If at first you don't succeed, destroy all evidence that you tried.");
        snarks.add("If at first you don't succeed, try bowling!");
        snarks.add("Please don't let your quiz scores define you as a person.");

        snarks.add("Looks like you've got all your ducks in a....crowd.");
        snarks.add("Failure is the condiment that gives success its flavor.");
        snarks.add("Oof. This is not your area of expertise.");
        snarks.add("Some days, everything goes right. Today is not one of those days.");

        snarks.add("Houston, we have a problem.");
        snarks.add("'Tis better to have played and lost than never to have played at all.");
        snarks.add("If your score were weather, it would be fifty degrees and partly cloudy.");
        snarks.add("That participation ribbon will look great hanging on your mantle.");
        snarks.add("Trying again is highly encouraged!");
        snarks.add("Ooof. That was rough.");
        snarks.add("Could be worse. At least it's not raining.");

        snarks.add("A miss is as good as a mile, but to be clear: That was a miss.");
        snarks.add("You have a Titanic intellect in a world full of icebergs.");
        snarks.add("Average (adj) - mediocre; not very good.");
        snarks.add("If your score were a color, it would be beige.");
        snarks.add("Don't sweat it. You'll get it next time!");
        snarks.add("You can do better!");

        snarks.add("You're off to astonish the world with more feats of ade-quata-quaticism.");
        snarks.add("If you were an optimist, you'd say that score was half full.");
        snarks.add("That was some high quality middle-of-the-road work right there.");
        snarks.add("Keep up that kind of performance and you'll go places...eventually.");
        snarks.add("I see what you did there. If you can't beat 'em, join 'em!");
        snarks.add("That was...okay, I guess.");
        snarks.add("Of all the scores you could possibly have achieved, that was certainly one of them.");

        snarks.add("You came. You saw. You conquered.");
        snarks.add("The altitude of your aptitude is astounding!");
        snarks.add("9/10 dentists agree: You are the BEST!");
        snarks.add("Heavens to Betsy! That was a special performance right there.");
        snarks.add("Albert Einstein called. He wants his brain back.");
        snarks.add("That was pretty good, but no one remembers second best.");
        snarks.add("Heard all your friends call you 'the smart one.'");

        snarks.add("What's it like to be perfect?");
        snarks.add("That right there was the work of a superior intellect.");
        snarks.add("I bet you fill out crossword puzzles in pen.");
        snarks.add("Welcome to perfect town. Population: You!");
        snarks.add("I bet the teacher used your tests as the answer key in high school.");
        snarks.add("Is this the greatest moment of your life?");
        snarks.add("Nailed it! Take a victory lap.");
    }

    public String getRandomSnark(int percent) {
        if (snarks.size()==0) populateSnarks();

        int startRng, endRng;
        if (percent <= 10) {
            startRng=0; endRng=3;

        } else if (percent <= 20) {
            startRng=4; endRng=7;

        } else if (percent <= 30) {
            startRng=8; endRng=11;

        } else if (percent <= 40) {
            startRng=13; endRng=16;

        } else if (percent <= 50) {
            startRng=17; endRng=20;

        } else if (percent <= 60) {
            startRng=21; endRng=27;

        } else if (percent <= 70) {
            startRng=28; endRng=33;

        } else if (percent <= 80) {
            startRng=34; endRng=40;

        } else if (percent <= 90) {
            startRng=41; endRng=47;

        } else {
            startRng=48; endRng=54;
        }

        Random rand = new Random();
        int randIdx = startRng + rand.nextInt((endRng - startRng) + 1);

        return snarks.get(randIdx);
    }
}
