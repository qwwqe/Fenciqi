package com.bonzimybuddy.fenciqi;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class describes an Object that can be used in conjunction with
 * an arbitrary lexicon to tokenize arbitrary text. This being the case,
 * however, it is designed specifically with Mandarin text in mind, and
 * therefore the heuristics employed in the tokenization process will
 * likely have little efficacy applied to other languages.
 *
 * The general tokenization algorithm, as well as the various heuristics
 * employed in disambiguating tokenizations of apparently equal likelihood,
 * are adopted from MMSEG, written by 蔡志浩 (Chih-Hao Tsai). Both are
 * described below.
 *
 * Tokenization is chiefly accomplished via a 3-Depth Maximum-Matching
 * Algorithm, the basic axiom of which is that given a string of characters
 * beginning at N, the most likely tokenization is the longest 3-word
 * sequence beginning at N. This depth is not fixed, and can be adjusted
 * as the user pleases, keeping in mind that deeper is not necessarily
 * better.
 *
 * When multiple 3-word sequences of equal length are found, various
 * heuristics are then employed to resolve disambiguation. These are as
 * implemented as follows and applied in the order described.
 *
 * 1) Greatest average word length
 * 2) Smallest variance of word lengths
 * 3) Largest sum of morphemic freedom of single-character words.
 *    Morphemic freedom here is approximated by frequency count.
 */
public class Tokenizer {
    private Trie lexicon;
    private String separator = " ";
    private boolean simple = false;
    private int maxDepth = 3;
    private String language = "zh-tw";

    // TODO: improve ignorant sentence delimitation
    private static HashMap<String, Character> sentenceDelimiters = new HashMap<>();
    static {
        sentenceDelimiters.put("zh-tw", '。');
    }

    public Tokenizer() {
    }

    public Tokenizer(String language) {
        language = language;
    }

    /**
     * Load a Trie as the Tokenizer's lexicon.
     * @param t
     */
    public void loadLexicon(Trie t) {
        lexicon = t;
    }

    /**
     * Tokenize a blob of text.
     * @param text
     * @return
     */
    public Corpus tokenize(String text) {
        int textOffset = 0;
        //ArrayList<String> tokenizedText = new ArrayList<>();
        Corpus corpus = new Corpus();
        while(textOffset < text.length()) {
            // chunk contiguous sequences of non-lexical entries.
            // this could probably be implemented elsewise to avoid
            // the additional search at the start of each word sequence.
            int j = textOffset;
            for(; j < text.length() && lexicon.find(String.valueOf(text.charAt(j))) == null; j++);
            if(j != textOffset) {

                /*-- PORT TO CORPUS --*/
                // TODO: CORRECT SENTENCE DELIMITATION (everything non-lexical here is currently considered a delimiter)
                //tokenizedText.add(text.substring(textOffset, j));
                corpus.addWord(text.substring(textOffset, j), false, true);
                /*-- PORT TO CORPUS --*/

                textOffset = j;
                if(textOffset >= text.length())
                    break;
            }

            // find all possible sequences beginning at textOffset(=j)
            ArrayList<WordNode> wordNodes = new ArrayList<>();
            for(; j < text.length(); j++) {
                String seq = text.substring(textOffset, j);
                Integer freq = lexicon.find(seq);
                if(freq == null)
                    break;
                else if(freq >= 0)
                    wordNodes.add(new WordNode(seq, freq, 1, textOffset, null));
            }

            // if no sequences exist which begin with textOffset, simply add textOffset and carry on
            if(wordNodes.size() == 0) {

                /*-- PORT TO CORPUS --*/
                // TODO: SAME AS ABOVE
                //tokenizedText.add(String.valueOf(text.charAt(textOffset)));
                if(text.charAt(textOffset) == sentenceDelimiters.get(language))
                    corpus.addWord(String.valueOf(text.charAt(textOffset)), false, true);
                else
                    corpus.addWord(String.valueOf(text.charAt(textOffset)), false, false);
                /*-- PORT TO CORPUS --*/

                textOffset += 1;
                continue;
            }

            // simple maximum matching mode
            if(simple) {
                WordNode longest = wordNodes.get(0);
                WordNode node;
                for(int k = 1; k < wordNodes.size(); k++) {
                    node = wordNodes.get(k);
                    if(node.length > longest.length)
                        longest = node;
                }

                /*-- PORT TO CORPUS --*/
                //tokenizedText.add(longest.word);
                corpus.addWord(longest.word);
                /*-- PORT TO CORPUS --*/

                textOffset += longest.length;
                continue;
            }

            // find all sequences containing maxDepth words, in addition to all shorter sequences
            // truncated by a single-character item that is both non-prefixal (null frequency) and non-lexical (ie, punctuation).
            ArrayList<WordNode> viableNodes = new ArrayList<>();
            for(int k = 0; k < wordNodes.size(); k++) {
                WordNode node = wordNodes.get(k);
                if(node.depth >= maxDepth)
                    break;

                int nextOffset = node.index + node.length;
                for(int i = nextOffset + 1; i < text.length(); i++) {
                    String s = text.substring(nextOffset, i);
                    Integer freq = lexicon.find(s);
                    if(freq == null) {
                        if(s.length() == 1) // single-character, non-prefixal (null frequency), non-lexical item
                            viableNodes.add(node);
                        break;
                    } else if(freq >= 0) {
                        WordNode newNode = new WordNode(s, freq, node.depth + 1, nextOffset, node);
                        if(newNode.depth == maxDepth)
                            viableNodes.add(newNode);
                        wordNodes.add(newNode);
                    }
                }
            }

            // find longest combinations and largest average wordlength
            if(viableNodes.size() > 1) {
                ArrayList<WordNode> tempNodes = new ArrayList<>();
                int longestLength = 0;
                float largestAverage = 0;
                for(WordNode node: viableNodes) {
                    if(node.sequenceLength > longestLength) {
                        longestLength = node.sequenceLength;
                        largestAverage = (float) node.sequenceLength / (float) node.sequenceSize;
                        tempNodes = new ArrayList<>();
                        tempNodes.add(node);
                    } else if(node.sequenceLength == longestLength) {
                        float averageWordLength = (float) node.sequenceLength / (float) node.sequenceSize;
                        if(averageWordLength > largestAverage) {
                            largestAverage = averageWordLength;
                            tempNodes = new ArrayList<>();
                            tempNodes.add(node);
                        } else if(averageWordLength == largestAverage) {
                            tempNodes.add(node);
                        }
                    }
                }
                viableNodes = tempNodes;
            }

            // find smallest variance of word lengths and greatest single-character morphemic freedom
            if(viableNodes.size() > 1) {
                ArrayList<WordNode> tempNodes = new ArrayList<>();
                Float smallestVariance = null;
                int greatestFreedom = 0;
                for(WordNode tailNode: viableNodes) {
                    float averageLength = (float) tailNode.sequenceLength / (float) tailNode.sequenceSize;
                    float squaredDistanceSum = 0;
                    int morphemicFreedomSum = 0;

                    // calculate variance and summate single-character morphemic freedom
                    for(WordNode node = tailNode; node != null; node = node.prev) {
                        float distance = node.length - averageLength;
                        squaredDistanceSum += (distance * distance);

                        if(node.length == 1)
                            morphemicFreedomSum += node.freq;
                    }
                    float variance = (float) squaredDistanceSum / (float) tailNode.sequenceSize;

                    // prune
                    if(smallestVariance == null || variance < smallestVariance) {
                        smallestVariance = variance;
                        greatestFreedom = morphemicFreedomSum;
                        tempNodes = new ArrayList<>();
                        tempNodes.add(tailNode);
                    } else if (variance == smallestVariance) {
                        if(morphemicFreedomSum > greatestFreedom) {
                            greatestFreedom = morphemicFreedomSum;
                            tempNodes = new ArrayList<>();
                            tempNodes.add(tailNode);
                        }
                        /* ONLY ONE WORD SURVIVES!!! */
                        // else if(morphemicFreedomSum == greatestFreedom) {
                        //     tempNodes.add(tailNode);
                        // }
                    }
                }
                viableNodes = tempNodes;
            }

            WordNode tailNode = viableNodes.get(0);
            ArrayList<String> words = new ArrayList<>();
            for(WordNode node = tailNode; node != null; node = node.prev)
                words.add(node.word);
            for(int i = words.size() - 1; i >= 0; i--) {

                /*-- PORT TO CORPUS --*/
                //tokenizedText.add(words.get(i));
                corpus.addWord(words.get(i));
                /*-- PORT TO CORPUS --*/
            }

            textOffset += tailNode.sequenceLength;
        }

        /*-- PORT TO CORPUS --*/
        //return(tokenizedText);
        return(corpus);
        /*-- PORT TO CORPUS --*/
    }

    private class WordNode {
        String word;
        int index;
        int freq;
        WordNode prev;
        int depth;
        int length;

        int sequenceLength;
        int sequenceSize;

        public WordNode(String w, int f, int d, int i, WordNode p) {
            word = w;
            freq = f;
            prev = p;
            index = i;
            depth = d;

            length = w.length();
            if(p != null) {
                sequenceLength = length + p.sequenceLength;
                sequenceSize = 1 + p.sequenceSize;
            } else {
                sequenceLength = length;
                sequenceSize = 1;
            }
        }
    }
}
