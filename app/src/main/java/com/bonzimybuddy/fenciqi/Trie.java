package com.bonzimybuddy.fenciqi;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class Trie {
    // FIELD:
    //   0   FREQ
    //   1   # OF CHILDREN
    //   2   VALUE OF CHILD #1
    //   3   RELATIVE OFFSET OF CHILD #1
    //   ...
    //   N+1 VALUE OF CHILD N
    //   N+2 RELATIVE OFFSET OF CHILD N
    //
    private static final int avgFieldSize = 4;
    private static final int rootFieldSize = 2;

    private LoaderTrie loaderTrie;
    private int[] trieArray;

    public Trie() {
        loaderTrie = new LoaderTrie();
    }

    public void loadWords(ArrayList<String> words, ArrayList<Integer> freqs) {
        loaderTrie.buildTrie(words, freqs);
        trieArray = loaderTrie.dumpTrieArray();
    }

    public void loadTrieArray(int[] t) {
        trieArray = t;
    }

    public int[] getTrieArray() {
        return trieArray;
    }

    /**
     * Finds a string in the trie and returns its frequency value.
     *
     * A non-negative frequency indicates existence of the given string,
     * a negative frequency indicates existence of the given string as a
     * prefix (ie, a morphemically bound prefix), and a null frequency
     * indicates the total non-existence of the given string.
     */
    public Integer find(String s) {
        int fieldIndex = 0;
        int childRowIndex = 0;
        int c;
        for(int i = 0; i < s.length(); i++) {
            c = (int) s.charAt(i);
            childRowIndex = findInField(c, fieldIndex);
            if(childRowIndex < 0)
                return null;

            //System.out.format("\t%10d CHAR: %c\n", childRowIndex, trieArray[childRowIndex]);
            //System.out.format("\t%10d OFFS: %d\n", childRowIndex + 1, trieArray[childRowIndex+1]);
            fieldIndex = fieldIndex + trieArray[childRowIndex + 1]; // offset to child field
            //System.out.format("\t%10d FREQ: %d\n\n", fieldIndex, trieArray[fieldIndex]);
        }

        if(trieArray[fieldIndex] >= 0)
            return trieArray[fieldIndex];
        else if(trieArray[fieldIndex + 1] > 0)
            return -1;
        else
            return null;
    }

    private int findInField(int c, int fieldIndex) {
        int kids = trieArray[fieldIndex+1];
        int frameStart = fieldIndex + 2;
        int frameEnd = frameStart + kids * 2 - 2;

        if(kids == 0)
            return -1;

        int i;
        while(frameStart <= frameEnd) {
            i = (frameStart + frameEnd) / 4 * 2;

            if(trieArray[i] == c)
                return i;
            else if(trieArray[i] > c)
                frameEnd = i - 2;
            else
                frameStart = i + 2;
        }

        return -1;

    }

    private class LoaderTrie {
        private Node rootNode;
        private int nodeCount;
        private int entryCount;

        public LoaderTrie() {
            rootNode = new Node();
            nodeCount = 1;
            entryCount = 0;
        }

        public void buildTrie(ArrayList<String> s, ArrayList<Integer> f) {
            for(int i = 0; i < s.size() && i < f.size(); i++)
                insert(s.get(i), f.get(i));
        }

        public int[] dumpTrieArray() {
            int[] trieArray = new int[rootFieldSize + ((nodeCount - 1) * avgFieldSize)];

            updateDescendantsCount(rootNode);
            dumpTrieArrayWalk(trieArray, 0, rootNode);

            return trieArray;
        }

        public int getNodeCount() {
            return nodeCount;
        }

        public int getEntryCount() {
            return entryCount;
        }

        private int updateDescendantsCount(Node n) {
            n.numDescendants = n.children.size();
            for(Iterator<Node> iter = n.children.values().iterator(); iter.hasNext(); )
                n.numDescendants += updateDescendantsCount(iter.next());

            return n.numDescendants;
        }

        private int dumpTrieArrayWalk(int[] a, int i, Node n) {
            a[i] = n.freq;
            a[i+1] = n.children.size();
            i += 2;

            int fieldOffset = 2 + n.children.size() * 2;
            Node tempNode;
            for(Iterator<Node> iter = n.children.values().iterator(); iter.hasNext(); i += 2) {
                tempNode = iter.next();
                a[i] = tempNode.value;
                a[i+1] = fieldOffset;

                fieldOffset += 2 + tempNode.numDescendants * avgFieldSize;
            }

            for(Iterator<Node> iter = n.children.values().iterator(); iter.hasNext(); )
                i = dumpTrieArrayWalk(a, i, iter.next());

            return i;
        }

        private void insert(String s, Integer f) {
            Node node = rootNode;
            Node newNode;

            int i;
            for(i = 0; i < s.length(); i++) {
                newNode = node.children.get((int) s.charAt(i));
                if(newNode != null)
                    node = newNode;
                else
                    break;
            }

            if(i < s.length())
                entryCount++;

            for(; i < s.length(); i++) {
                newNode = new Node();
                newNode.value = (int) s.charAt(i);
                nodeCount++;
                node.children.put(newNode.value, newNode);
                node = newNode;
            }

            node.freq = f;
        }

        private class Node implements Comparable<Node>{
            private int value;
            private int freq;
            private int numDescendants;
            private TreeMap<Integer, Node> children;

            public Node() {
                value = 0;
                freq = -1;
                numDescendants = 0;
                children = new TreeMap<>();
            }

            public int compareTo(Node n) {
                return value - n.value;
            }

            public boolean equals(Node n) {
                return value == n.value;
            }
        }
    }
}
