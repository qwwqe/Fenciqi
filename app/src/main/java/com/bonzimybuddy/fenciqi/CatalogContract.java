package com.bonzimybuddy.fenciqi;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class CatalogContract {

    // content://AUTHORITY/lexica
    // content://AUTHORITY/lexica/#
    // content://AUTHORITY/lexica/#/words
    // content://AUTHORITY/lexica/#/words/#
    // content://AUTHORITY/vocabularies
    // content://AUTHORITY/vocabularies/#
    // content://AUTHORITY/vocabularies/#/words
    // content://AUTHORITY/vocabularies/#/words/#
    // content://AUTHORITY/corpora
    // content://AUTHORITY/corpora/#
    // content://AUTHORITY/corpora/#/words
    // content://AUTHORITY/corpora/#/words/#
    // content://AUTHORITY/languages
    // content://AUTHORITY/languages/#

    private CatalogContract() {}

    public static final String AUTHORITY = "com.bonzimybuddy.fenciqi.catalog";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    //  languages:
    //    long _id
    //    String name
    public static abstract class Languages implements BaseColumns {
        public static final String TABLE_NAME = "languages";
        public static final String COLUMN_NAME = "name";

        public static final String CONTENT_URI_SEGMENT = "languages";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
    }

    //  lexica:
    //    long _id
    //    String name
    //    String uri
    //    long language_id
    //    String language     (read only)
    //    int entries           (read only)
    public static abstract class Lexica implements BaseColumns {
        public static final String TABLE_NAME = "lexica";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_LANGUAGE_ID = "language_id";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_ENTRIES = "entries";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
    }

    //  lexicon_words:
    //    long _id
    //    long word_id        (read only)
    //    long lexicon_id     (read only)
    //    String word
    //    String lexicon      (read only)
    //    int freq            (write only)
/*
    public static abstract class LexiconWords implements BaseColumns {
        public static final String TABLE_NAME = "lexicon_words";
        public static final String COLUMN_WORD_ID = "word_id";
        public static final String COLUMN_LEXICON_ID = "lexicon_id";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_LEXICON = "lexicon";
        public static final String COLUMN_FREQ = "freq";

        public static final String CONTENT_URI_SEGMENT = "words";
        public static Uri buildContentUri(long lexiconId) {
            return ContentUris.withAppendedId(Lexica.CONTENT_URI, lexiconId).buildUpon().appendPath(CONTENT_URI_SEGMENT).build();
        }
    }
*/


    //  vocabularies:
    //    long _id
    //    String name
    //    String uri
    //    long language_id
    //    String language     (read only)
    //    int count           (read only)
    public static abstract class Vocabularies implements BaseColumns {
        public static final String TABLE_NAME = "vocabularies";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_LANGUAGE_ID = "language_id";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_COUNT = "count";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
    }

    //  vocabulary_words:
    //    long _id
    //    int date
    //    long word_id
    //    long vocabulary_id
    //    String word
    //    String vocabulary   (read only)
    public static abstract class VocabularyWords implements BaseColumns {
        public static final String TABLE_NAME = "vocabulary_words";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WORD_ID = "word_id";
        public static final String COLUMN_VOCABULARY_ID = "vocabulary_id";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_VOCABULARY = "vocabulary";

        public static final String CONTENT_URI_SEGMENT = "words";
        public static Uri buildContentUri(long vocabularyId) {
            return ContentUris.withAppendedId(Vocabularies.CONTENT_URI, vocabularyId).buildUpon().appendPath(CONTENT_URI_SEGMENT).build();
        }
    }

    //  corpora:
    //    long _id
    //    String name
    //    String uri
    //    int md5
    //    long language_id
    //    String language     (read only)
    //    int count           (read only)
    public static abstract class Corpora implements BaseColumns {
        public static final String TABLE_NAME = "corpora";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_MD5 = "md5";
        public static final String COLUMN_LANGUAGE_ID = "language_id";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_COUNT = "count";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_CORPUS = "corpus";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
    }

    //  corpus_words:
    //    long _id
    //    int pos
    //    long word_id
    //    long corpus_id
    //    String word
    //    String corpus       (read only)
    public static abstract class CorpusWords implements BaseColumns {
        public static final String TABLE_NAME = "corpus_words";
        public static final String COLUMN_POS = "pos";
        public static final String COLUMN_WORD_ID = "word_id";
        public static final String COLUMN_CORPUS_ID = "corpus_id";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_CORPUS = "corpus";

        public static final String CONTENT_URI_SEGMENT = "words";
        public static Uri buildContentUri(long corpusId) {
            return ContentUris.withAppendedId(Corpora.CONTENT_URI, corpusId).buildUpon().appendPath(CONTENT_URI_SEGMENT).build();
        }
    }
}
