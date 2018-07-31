package com.bonzimybuddy.fenciqi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class Corpus implements Iterable<CorpusWord> {
    private String mCorpusName;
    private String mLanguageName;
    private String mLanguageId;
    ArrayList<CorpusWord> mText;

    public Corpus() {

    }

    public Corpus(String name) {
        mCorpusName = name;
    }

    public Corpus(String name, String languageName, String languageId) {
        mCorpusName = name;
        mLanguageName = languageName;
        mLanguageId = languageId;
    }

    public void setCorpusName(String name) {
        mCorpusName = name;
    }

    public String getName() {
        return mCorpusName;
    }

    public void setLanguageName(String languageName) {
        mLanguageName = languageName;
    }

    public String getLanguageName() {
        return mLanguageName;
    }

    public void setLanguageId(String id) {
        mLanguageId = id;
    }

    public String getLanguageId() {
        return mLanguageId;
    }

    public CorpusWord addWord(String word) {
        return addWord(word, true, false);
    }

    public CorpusWord addWord(String word, boolean lexical, boolean sentenceDelim) {
        CorpusWord corpusWord = new CorpusWord(word, mText.size(), lexical, sentenceDelim);
        mText.add(corpusWord);

        return corpusWord;
    }

    public ArrayList<String> getTextAsStringArrayList() {
        ArrayList<String> words = new ArrayList<>();
        for(CorpusWord corpusWord : mText) {
            words.add(corpusWord.toString());
        }
        return words;
    }

    public ArrayList<CorpusWord> getTextAsCorpusWordArrayList() {
        return new ArrayList<CorpusWord>(mText);
    }

    // TODO: MAKE THIS MORE EFFICIENT
    /**
     * return the sentence contained the specified word. all sequences of sentence delimiters
     * are considered to *end* a given sentence
     * @param corpusWord
     * @return
     */
    public String getSentence(CorpusWord corpusWord) {
        if(corpusWord == null)
            return null;

        String sentence = "";

        CorpusWord firstWord = getPreviousWord(corpusWord);

        // find first word
        while(firstWord != null && !firstWord.isSentenceDelim())
            firstWord = getPreviousWord(firstWord);
        if(firstWord == null)
            firstWord = mText.get(0);

        // concatenate string
        while(firstWord != null && !firstWord.isSentenceDelim()) {
            sentence += firstWord;
            firstWord = getNextWord(firstWord);
        }
        // include trailing delimiters
        while(firstWord != null && firstWord.isSentenceDelim()) {
            sentence += firstWord;
            firstWord = getNextWord(firstWord);
        }

        return sentence;
    }

    public CorpusWord getWord(int pos) {
        return mText.get(pos);
    }

    public CorpusWord getPreviousWord(CorpusWord corpusWord) {
        int pos = corpusWord.getPos();
        if(pos == 0)
            return null;
        else
            return mText.get(pos - 1);
    }

    public CorpusWord getNextWord(CorpusWord corpusWord) {
        int pos = corpusWord.getPos();
        if(pos == mText.size() - 1)
            return null;
        else
            return mText.get(pos + 1);
    }

    public ListIterator<CorpusWord> corpusWordIterator() {
        return mText.listIterator();
    }

    @Override
    public Iterator<CorpusWord> iterator() {
        return mText.listIterator();
    }

}
