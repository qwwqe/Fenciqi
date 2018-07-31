package com.bonzimybuddy.fenciqi;

public class CorpusWord {
    private String word;
    private int pos;
    boolean lexical;
    boolean sentenceDelim;

    public CorpusWord(String word, int pos, boolean lexical, boolean sentenceDelim) {
        word = word;
        pos = pos;
        lexical = lexical;
        sentenceDelim = sentenceDelim;
    }

    @Override
    public String toString() {
        return word;
    }

    public int getPos() {
        return pos;
    }

    public boolean islexical() {
        return lexical;
    }

    public boolean isSentenceDelim() {
        return sentenceDelim;
    }
}
