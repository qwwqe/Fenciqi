package com.bonzimybuddy.fenciqi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

public class DiskTrie {
    private DiskTrie() {}

    public static void dumpToDisk(Trie trie, String filename) throws Exception {
        int[] trieArray = trie.getTrieArray();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(trieArray.length * 4 + 4);
        byteBuffer.putInt(trieArray.length);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(trieArray);
        byteBuffer.position(0);

        FileChannel fc = new FileOutputStream(filename).getChannel();
        fc.write(byteBuffer);
        fc.force(false);
        fc.close();
    }

    public static Trie loadFromDisk(String filename) throws Exception {
        Trie trie = new Trie();
        FileChannel fc = new FileInputStream(filename).getChannel();
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);

        while(lengthBuffer.remaining() > 0)
            fc.read(lengthBuffer);
        int length = lengthBuffer.getInt(0);

        int[] trieArray = new int[length];
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length * 4);
        while(byteBuffer.remaining() > 0 && fc.read(byteBuffer) > 0);

        byteBuffer.flip();
        byteBuffer.asIntBuffer().get(trieArray);

        trie.loadTrieArray(trieArray);

        return trie;
    }
}
