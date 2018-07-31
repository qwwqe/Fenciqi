package com.bonzimybuddy.fenciqi;

import android.net.Uri;
import android.provider.BaseColumns;

public final class DatabaseContract {
    private DatabaseContract() {}

    public static final String[] CREATE_TABLE_STATEMENTS = {
        Languages.CREATE_TABLE, // IETF language tags
        Words.CREATE_TABLE,
        Lexica.CREATE_TABLE,
        LexiconWords.CREATE_TABLE,
        Vocabularies.CREATE_TABLE,
        VocabularyWords.CREATE_TABLE,
        Corpora.CREATE_TABLE,
        CorpusWords.CREATE_TABLE
    };

    public static final String[] CREATE_VIEW_STATEMENTS = {
            //LexicaView.CREATE_VIEW,
            VocabulariesView.CREATE_VIEW
    };

    /*
    CREATE TABLE languages (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL);
     */
    public static abstract class Languages implements BaseColumns {
        public static final String TABLE_NAME = "languages";
        public static final String COLUMN_NAME = "name";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT UNIQUE NOT NULL" + ");";
    }

    /*
    CREATE TABLE words (_id INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT NOT NULL, freq INTEGER DEFAULT 0,
        language INTEGER NOT NULL REFERENCES languages, UNIQUE(word, language));
     */
    public static abstract class Words implements BaseColumns {
        public static final String TABLE_NAME = "words";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_FREQ = "freq";
        public static final String COLUMN_LANGUAGE = "language";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORD + " TEXT NOT NULL, " +
                COLUMN_FREQ + " INTEGER DEFAULT 0, " +
                COLUMN_LANGUAGE + " INTEGER NOT NULL REFERENCES " + Languages.TABLE_NAME + ", " +
                "UNIQUE(" + COLUMN_WORD + ", " + COLUMN_LANGUAGE + "));";
    }

    /*
    CREATE TABLE lexica (_id INTEGER PRIMARY KEY AUTOINCREMENT, uri TEXT UNIQUE NOT NULL, name TEXT UNIQUE NOT NULL,
        language INTEGER NOT NULL REFERENCES languages, entries INTEGER NOT NULL);
     */
    public static abstract class Lexica implements BaseColumns {
        public static final String TABLE_NAME = "lexica";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_ENTRIES = "entries";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_URI + " TEXT UNIQUE NOT NULL, " +
                COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_LANGUAGE + " INTEGER NOT NULL REFERENCES " + Languages.TABLE_NAME + ", " +
                COLUMN_ENTRIES + " INTEGER NOT NULL);";
    }

    /*
    CREATE TABLE lexicon_words (_id INTEGER PRIMARY KEY AUTOINCREMENT, word INTEGER NOT NULL REFERENCES words,
        lexicon INTEGER NOT NULL REFERENCES lexica, UNIQUE(word, lexicon));
     */
    public static abstract class LexiconWords implements BaseColumns {
        public static final String TABLE_NAME = "lexicon_words";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_LEXICON = "lexicon";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORD + " INTEGER NOT NULL REFERENCES " + Words.TABLE_NAME + " ON DELETE CASCADE, " +
                COLUMN_LEXICON + " INTEGER NOT NULL REFERENCES " + Lexica.TABLE_NAME + " ON DELETE CASCADE, " +
                "UNIQUE( " + COLUMN_WORD + ", " + COLUMN_LEXICON + "));";
    }

    // TODO: add uri, language, language_id fields (as per CatalogContract)
    /*
    CREATE VIEW lexica_view AS
        SELECT lexica._id, lexica.name, count(lexicon) AS "count" FROM lexica
        LEFT JOIN lexicon_words ON lexica._id = lexicon_words.lexicon
        GROUP BY lexica._id, lexica.name
        ORDER BY lexica.name;
     */
/*
    public static abstract class LexicaView {
        public static final String VIEW_NAME = "lexica_view";
        //public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ENTRIES = "entries";

        public static final String CREATE_VIEW = "CREATE VIEW " + VIEW_NAME +
                " AS SELECT " + Lexica.TABLE_NAME + "." + Lexica._ID + ", " +
                Lexica.TABLE_NAME + "." + Lexica.COLUMN_NAME +
                ", count(" + LexiconWords.COLUMN_LEXICON + ") AS \"" + COLUMN_ENTRIES +
                "\" FROM " + Lexica.TABLE_NAME +
                " LEFT JOIN " + LexiconWords.TABLE_NAME + " ON " +
                Lexica.TABLE_NAME + "." + Lexica._ID + " = " + LexiconWords.TABLE_NAME + "." + LexiconWords.COLUMN_LEXICON +
                " GROUP BY " + Lexica.TABLE_NAME + "." + Lexica._ID + ", " + Lexica.TABLE_NAME + "." + Lexica.COLUMN_NAME +
                " ORDER BY " + Lexica.TABLE_NAME + "." + Lexica.COLUMN_NAME + ";";
    }
*/

    // TODO: implement (as per CatalogContract)
    public static abstract class LexiconWordsView { }

    /*
    CREATE TABLE vocabularies (_id INTEGER PRIMARY KEY AUTOINCREMENT, uri TEXT UNIQUE NOT NULL, name TEXT UNIQUE NOT NULL,
        language INTEGER NOT NULL REFERENCES languages);
     */
    public static abstract class Vocabularies implements BaseColumns {
        public static final String TABLE_NAME = "vocabularies";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LANGUAGE = "language";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_URI + " TEXT UNIQUE NOT NULL, " +
                COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_LANGUAGE + " INTEGER NOT NULL REFERENCES " + Languages.TABLE_NAME + ");";
    }

    /*
    CREATE TABLE vocabulary_words (_id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER,
        word INTEGER NOT NULL REFERENCES words, vocabulary INTEGER NOT NULL REFERENCES vocabularies,
        UNIQUE(word, vocabulary));
     */
    public static abstract class VocabularyWords implements BaseColumns {
        public static final String TABLE_NAME = "vocabulary_words";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_VOCABULARY = "vocabulary";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " INTEGER, " +
                COLUMN_WORD + " INTEGER NOT NULL REFERENCES " + Words.TABLE_NAME + " ON DELETE CASCADE, " +
                COLUMN_VOCABULARY + " INTEGER NOT NULL REFERENCES " + Vocabularies.TABLE_NAME + " ON DELETE CASCADE, " +
                "UNIQUE(" + COLUMN_WORD + ", " + COLUMN_VOCABULARY + "));";
    }

    // TODO: implement (as per CatalogContract)
    public static abstract class VocabulariesView {
        public static final String VIEW_NAME = "vocabularies_view";
        //public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COUNT = "count";

        public static final String CREATE_VIEW = "CREATE VIEW " + VIEW_NAME +
                " AS SELECT " + Vocabularies.TABLE_NAME + "." + Vocabularies._ID + ", " +
                Vocabularies.TABLE_NAME + "." + Vocabularies.COLUMN_NAME +
                ", count(" + VocabularyWords.COLUMN_VOCABULARY + ") AS \"" + COLUMN_COUNT +
                "\" FROM " + Vocabularies.TABLE_NAME +
                " LEFT JOIN " + VocabularyWords.TABLE_NAME + " ON " +
                Vocabularies.TABLE_NAME + "." + Vocabularies._ID + " = " + VocabularyWords.TABLE_NAME + "." + VocabularyWords.COLUMN_VOCABULARY +
                " GROUP BY " + Vocabularies.TABLE_NAME + "." + Vocabularies._ID + ", " + Vocabularies.TABLE_NAME + "." + Vocabularies.COLUMN_NAME +
                " ORDER BY " + Vocabularies.TABLE_NAME + "." + Vocabularies.COLUMN_NAME + ";";
    }

    // TODO: implement (as per CatalogContract)
    public static abstract class VocabularyWordsView { }

    /*
    CREATE TABLE corpora (_id INTEGER PRIMARY KEY AUTOINCREMENT, uri TEXT UNIQUE NOT NULL, name TEXT UNIQUE NOT NULL,
        md5 INTEGER UNIQUE NOT NULL, language INTEGER NOT NULL REFERENCES languages);
     */
    public static abstract class Corpora implements BaseColumns {
        public static final String TABLE_NAME = "corpora";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MD5 = "md5";
        public static final String COLUMN_LANGUAGE = "language";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_URI + " TEXT UNIQUE NOT NULL, " +
                COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_MD5 + " INTEGER UNIQUE NOT NULL, " +
                COLUMN_LANGUAGE + " INTEGER NOT NULL REFERENCES " + Languages.TABLE_NAME + ");";
    }

    /*
    CREATE TABLE corpus_words (_id INTEGER PRIMARY KEY AUTOINCREMENT, pos INTEGER NOT NULL,
        corpus INTEGER NOT NULL REFERENCES corpora, word INTEGER NOT NULL REFERENCES words);
     */
    public static abstract class CorpusWords implements BaseColumns {
        public static final String TABLE_NAME = "corpus_words";
        public static final String COLUMN_POS = "pos";
        public static final String COLUMN_CORPUS = "corpus";
        public static final String COLUMN_WORD = "word";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_POS + " INTEGER NOT NULL, " +
                COLUMN_CORPUS + " INTEGER NOT NULL REFERENCES " + Corpora.TABLE_NAME + " ON DELETE CASCADE, " +
                COLUMN_WORD + " INTEGER NOT NULL REFERENCES " + Words.TABLE_NAME + ", " +
                "UNIQUE(" + COLUMN_POS + ", " + COLUMN_CORPUS + "));";
    }

    // TODO: implement (as per CatalogContract)
    public static abstract class CorporaView { }

    // TODO: implement (as per CatalogContract)
    public static abstract class CorpusWordsView { }
}
