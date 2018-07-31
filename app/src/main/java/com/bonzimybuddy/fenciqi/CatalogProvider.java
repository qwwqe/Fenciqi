package com.bonzimybuddy.fenciqi;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteProgram;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class CatalogProvider extends ContentProvider {
    private CatalogSQLiteHelper mCatalogHelper;
    private static final String DB_NAME = "catalog.db";
    private static final Integer DB_VERSION = 1;

    private SQLiteDatabase db;

    private static final int LEXICA = 0001;
    private static final int LEXICA_ID = 0002;
    //private static final int LEXICA_WORDS = 0003;
    //private static final int LEXICA_WORDS_ID = 0004;

    private static final int VOCABULARIES = 1001;
    private static final int VOCABULARIES_ID = 1002;
    private static final int VOCABULARIES_WORDS = 1003;
    private static final int VOCABULARIES_WORDS_ID = 1004;

    private static final int CORPORA = 2001;
    private static final int CORPORA_ID = 2002;
    private static final int CORPORA_WORDS = 2003;
    private static final int CORPORA_WORDS_ID = 2004;

    private static final int LANGUAGES = 3001;
    private static final int LANGUAGE_ID = 3002;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "lexica", LEXICA);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "lexica/#", LEXICA_ID);
        //sURIMatcher.addURI(CatalogContract.AUTHORITY, "lexica/#/words", LEXICA_WORDS);
        //sURIMatcher.addURI(CatalogContract.AUTHORITY, "lexica/#/words/#", LEXICA_WORDS_ID);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "vocabularies", VOCABULARIES);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "vocabularies/#", VOCABULARIES_ID);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "vocabularies/#/words", VOCABULARIES_WORDS);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "vocabularies/#/words/#", VOCABULARIES_WORDS_ID);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "corpora", CORPORA);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "corpora/#", CORPORA_ID);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "corpora/#/words", CORPORA_WORDS);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "corpora/#/words/#", CORPORA_WORDS_ID);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "languages", LANGUAGES);
        sURIMatcher.addURI(CatalogContract.AUTHORITY, "languages/#", LANGUAGE_ID);
    }

    public CatalogProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        db = mCatalogHelper.getWritableDatabase();

        switch(sURIMatcher.match(uri)) {
            case LEXICA_ID:
                rowsDeleted = db.delete(DatabaseContract.Lexica.TABLE_NAME, "_id = ?",
                                        new String[] {uri.getLastPathSegment()});
                break;
            case VOCABULARIES_ID:
                rowsDeleted = db.delete(DatabaseContract.Vocabularies.TABLE_NAME, "_id = ?",
                                        new String[] {uri.getLastPathSegment()});
            default:
                break;
        }

        if(rowsDeleted > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri finalUri = null;

        db = mCatalogHelper.getWritableDatabase();

        long id = -1;
        switch(sURIMatcher.match(uri)) {
            // VALUES: String uri, String name, int language
            case LEXICA:
                finalUri = CatalogContract.Lexica.CONTENT_URI;
                id = db.insert(DatabaseContract.Lexica.TABLE_NAME, null, values);
                break;

            // VALUES: String word, int freq
/*
            case LEXICA_WORDS:
                String lexiconId = uri.getPathSegments().get(1);
                finalUri = CatalogContract.LexiconWords.buildContentUri(Long.valueOf(lexiconId));

                long wordId;
                db.beginTransaction();
                try {
                    wordId = db.insertWithOnConflict(DatabaseContract.Words.TABLE_NAME,
                            null, values, db.CONFLICT_IGNORE);
                    if (wordId < 0) {
                        wordId = db.query(DatabaseContract.Words.TABLE_NAME,
                                new String[]{DatabaseContract.Words._ID},
                                DatabaseContract.Words.COLUMN_WORD + " = ?",
                                new String[]{values.getAsString(DatabaseContract.Words.COLUMN_WORD)},
                                null, null, null)
                                .getLong(0);
                    }

                    ContentValues lexiconWordValues = new ContentValues();
                    lexiconWordValues.put(DatabaseContract.LexiconWords.COLUMN_WORD, wordId);
                    lexiconWordValues.put(DatabaseContract.LexiconWords.COLUMN_LEXICON, lexiconId);
                    id = db.insertWithOnConflict(DatabaseContract.LexiconWords.TABLE_NAME,
                            null, lexiconWordValues, db.CONFLICT_IGNORE);
                } catch (SQLException e) {
                    Log.w("PROVIDER INSERTION", e);
                } finally {
                    db.endTransaction();
                }

                break;
*/

            // VALUES: String uri, String name, int language
            case VOCABULARIES:
                finalUri = CatalogContract.Vocabularies.CONTENT_URI;
                id = db.insert(DatabaseContract.Vocabularies.TABLE_NAME, null, values);
                break;

            // VALUES: String word, int date
            case VOCABULARIES_WORDS:
                String vocabularyId = uri.getPathSegments().get(1);
                finalUri = CatalogContract.VocabularyWords.buildContentUri(Long.valueOf(vocabularyId));

                long vocabWordId;
                db.beginTransaction();
                try {
                    vocabWordId = db.insertWithOnConflict(DatabaseContract.Words.TABLE_NAME,
                            null, values, db.CONFLICT_IGNORE);
                    if (vocabWordId < 0) {
                        vocabWordId = db.query(DatabaseContract.Words.TABLE_NAME,
                                new String[]{DatabaseContract.Words._ID},
                                DatabaseContract.Words.COLUMN_WORD + " = ?",
                                new String[]{values.getAsString(DatabaseContract.Words.COLUMN_WORD)},
                                null, null, null)
                                .getLong(0);
                    }

                    ContentValues vocabWordValues = new ContentValues();
                    vocabWordValues.put(DatabaseContract.VocabularyWords.COLUMN_WORD, vocabWordId);
                    vocabWordValues.put(DatabaseContract.VocabularyWords.COLUMN_VOCABULARY, vocabularyId);
                    id = db.insertWithOnConflict(DatabaseContract.VocabularyWords.TABLE_NAME,
                            null, vocabWordValues, db.CONFLICT_IGNORE);
                } catch (SQLException e) {
                    Log.w("PROVIDER INSERTION", e);
                } finally {
                    db.endTransaction();
                }

                break;

            // VALUES: Corpus corpus
            case CORPORA:
                finalUri = CatalogContract.Corpora.CONTENT_URI;

                // insert tokenized text into words table
                Corpus corpus = (Corpus) values.get(CatalogContract.Corpora.COLUMN_CORPUS);
                ArrayList<String> words = corpus.getTextAsStringArrayList();
                HashMap<String, String> wordIds = new HashMap<>();
                addAndRetrieveWords(corpus.getLanguageId(), words, wordIds);

                // insert corpus into corpora table and words into corpus_words table
                db.beginTransaction();

                try {
                    // insert corpus into corpora
                    ContentValues corpusValues = new ContentValues();
                    corpusValues.put(DatabaseContract.Corpora.COLUMN_NAME, corpus.getName());
                    corpusValues.put(DatabaseContract.Corpora.COLUMN_LANGUAGE, corpus.getLanguageId());
                    id = db.insertOrThrow(DatabaseContract.Corpora.TABLE_NAME, null, corpusValues);

                    // insert words into corpus_words
                    for(CorpusWord corpusWord : corpus) {
                        ContentValues corpusWordsValues = new ContentValues();
                        corpusWordsValues.put(DatabaseContract.CorpusWords.COLUMN_CORPUS, id);
                        corpusWordsValues.put(DatabaseContract.CorpusWords.COLUMN_POS, corpusWord.getPos());
                        corpusWordsValues.put(DatabaseContract.CorpusWords.COLUMN_WORD, wordIds.get(corpusWord.toString()));
                        db.insertOrThrow(DatabaseContract.CorpusWords.TABLE_NAME, null, corpusWordsValues);
                    }

                } catch (SQLException e) {
                    Log.w("PROVIDER INSERTION", e);
                } finally {
                    db.endTransaction();
                }

                break;

            // VALUES: String name
            case LANGUAGES:
                finalUri = CatalogContract.Languages.CONTENT_URI;
                id = db.insert(DatabaseContract.Languages.TABLE_NAME, null, values);
                break;

            default:
                break;
        }

        if (id > -1) {
            finalUri = ContentUris.withAppendedId(finalUri, id);
            getContext().getContentResolver().notifyChange(finalUri, null);
        } else {
            finalUri = null;
        }

        return finalUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String table;
        int itemCount = 0;

        db = mCatalogHelper.getWritableDatabase();

        switch(sURIMatcher.match(uri)) {
/*
            case LEXICA_WORDS:
                String lexiconId = uri.getPathSegments().get(1);

                db.beginTransaction();

                try {
                    Cursor c = db.query(DatabaseContract.Lexica.TABLE_NAME,
                            new String[] {DatabaseContract.Lexica.COLUMN_LANGUAGE},
                            "_id = ?", new String[] {lexiconId}, null, null, null);
                    c.moveToFirst();
                    String languageId = c.getString(0);

                    // first add actual words, then add lexicon_words
                    String word;
                    String freq;
                    for(ContentValues wordValues : values) {
                        word = wordValues.getAsString(CatalogContract.LexiconWords.COLUMN_WORD);
                        freq = wordValues.getAsString(CatalogContract.LexiconWords.COLUMN_FREQ);

                        if(null == freq)
                            freq = "";
                        if(null == word)
                            word = "";

                        String statement = "INSERT OR IGNORE INTO " + DatabaseContract.Words.TABLE_NAME + " (" +
                                DatabaseContract.Words.COLUMN_WORD + ", " +
                                DatabaseContract.Words.COLUMN_LANGUAGE + ", " +
                                DatabaseContract.Words.COLUMN_FREQ + ") VALUES (?, ?, ?);";
                        String[] args = new String[] { word, languageId, freq };
                        db.execSQL(statement, args);
                        //Log.w("sql bulk insert", statement);
                        //Log.w("args", args[0] + " " + args[1] + " " + args[2]);
                        //ContentValues baseWordValues = new ContentValues();
                        //baseWordValues.put(DatabaseContract.Words.COLUMN_WORD, word);
                        //baseWordValues.put(DatabaseContract.Words.COLUMN_LANGUAGE, languageId);
                        //baseWordValues.put(DatabaseContract.Words.COLUMN_FREQ, freq);
                        //long baseWordId = db.insertOrThrow(DatabaseContract.Words.TABLE_NAME, null,
                        //        baseWordValues);
                        //Log.w("bulk lexica insert", String.valueOf(baseWordId));
                        //c = db.query(DatabaseContract.Words.TABLE_NAME, null, null, null, null, null, null);
                        //while(c.moveToNext()) {
                        //    Log.w("bulk insert requery", c.getString(1));
                        //}
                        // TODO: if languageId was previously created using a user-chosen id, is the following
                        //       statement vulnerable to an injection attack?
                        try {
                            db.execSQL("INSERT INTO " + DatabaseContract.LexiconWords.TABLE_NAME + " (" +
                                            DatabaseContract.LexiconWords.COLUMN_WORD + ", " +
                                            DatabaseContract.LexiconWords.COLUMN_LEXICON + ") SELECT " +
                                            DatabaseContract.Words._ID + ", " + lexiconId + " FROM " + DatabaseContract.Words.TABLE_NAME +
                                            " WHERE " + DatabaseContract.Words.TABLE_NAME + "." + DatabaseContract.Words.COLUMN_WORD +
                                            " = ? AND " + DatabaseContract.Words.TABLE_NAME + "." + DatabaseContract.Words.COLUMN_LANGUAGE +
                                            " = " + languageId + ";",
                                    new String[]{word});
                            itemCount++;
                        } catch(SQLiteConstraintException e) {
                            Log.w("BULK INSERT", e);
                        }
                    }
                    // TODO: is this accurate?
                    getContext().getContentResolver().notifyChange(uri, null);
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    Log.w("PROVIDER INSERTION", e);
                } finally {
                    db.endTransaction();
                }
                break;
*/
            case VOCABULARIES_WORDS:
                String vocabularyId = uri.getPathSegments().get(1);

                db.beginTransaction();

                try {
                    Cursor c = db.query(DatabaseContract.Vocabularies.TABLE_NAME,
                            new String[] {DatabaseContract.Vocabularies.COLUMN_LANGUAGE},
                            "_id = ?", new String[] {vocabularyId}, null, null, null);
                    c.moveToFirst();
                    String languageId = c.getString(0);

                    // first add actual words, then add lexicon_words
                    String word;
                    String date;
                    for(ContentValues wordValues : values) {
                        word = wordValues.getAsString(CatalogContract.VocabularyWords.COLUMN_WORD);
                        date = wordValues.getAsString(CatalogContract.VocabularyWords.COLUMN_DATE);

                        if(null == date)
                            date = "";
                        if(null == word)
                            word = "";

                        String statement = "INSERT OR IGNORE INTO " + DatabaseContract.Words.TABLE_NAME + " (" +
                                DatabaseContract.Words.COLUMN_WORD + ", " +
                                DatabaseContract.Words.COLUMN_LANGUAGE + ") VALUES (?, ?);";
                        String[] args = new String[] { word, languageId };
                        db.execSQL(statement, args);
                        // TODO: if languageId was previously created using a user-chosen id, is the following
                        //       statement vulnerable to an injection attack?
                        try {
                            statement = "INSERT INTO " + DatabaseContract.VocabularyWords.TABLE_NAME + " (" +
                                    DatabaseContract.VocabularyWords.COLUMN_WORD + ", " +
                                    DatabaseContract.VocabularyWords.COLUMN_VOCABULARY + ") SELECT " +
                                    DatabaseContract.Words._ID + ", " + vocabularyId + " FROM " + DatabaseContract.Words.TABLE_NAME +
                                    " WHERE " + DatabaseContract.Words.TABLE_NAME + "." + DatabaseContract.Words.COLUMN_WORD +
                                    " = ? AND " + DatabaseContract.Words.TABLE_NAME + "." + DatabaseContract.Words.COLUMN_LANGUAGE +
                                    " = " + languageId + ";";
                            db.execSQL(statement, new String[]{word});
                            itemCount++;
                        } catch(SQLiteConstraintException e) {
                            Log.w("BULK INSERT", word);
                        }
                    }
                    // TODO: is this accurate?
                    getContext().getContentResolver().notifyChange(uri, null);
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    Log.w("PROVIDER INSERTION", e);
                } finally {
                    db.endTransaction();
                }
                break;
            case CORPORA:
                table = DatabaseContract.Corpora.TABLE_NAME;
                break;
            default:
                Log.w("UNHANDLED BULK INSERT", uri.toString());
                break;
        }

        return itemCount;
    }

    @Override
    public boolean onCreate() {
        mCatalogHelper = CatalogSQLiteHelper.getInstance(getContext(), DB_NAME, null, DB_VERSION);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        db = mCatalogHelper.getWritableDatabase();

        long id = -1;
        switch(sURIMatcher.match(uri)) {
            case LEXICA:
                cursor = db.query(DatabaseContract.Lexica.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case VOCABULARIES:
                cursor = db.query(DatabaseContract.VocabulariesView.VIEW_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }

        // TODO: handle cases for uri wildcards and invalid uris
        if(cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * helper to commit words to database and retrieve their ids. DO NOT CALL WITHIN A TRANSACTION
     * @param language language id
     * @param words words to commit/retrieve
     * @param wordIds empty HashMap to store word ids
     */
    private void addAndRetrieveWords(String language, final ArrayList<String> words, HashMap<String, String> wordIds) {
        db.beginTransaction();
        for (String word : words) {
            // attempt to insert word and retrieve new row id
            ContentValues wordValues = new ContentValues();
            wordValues.put(DatabaseContract.Words.COLUMN_WORD, word);
            wordValues.put(DatabaseContract.Words.COLUMN_LANGUAGE, language);
            long wordId = db.insertWithOnConflict(DatabaseContract.Words.TABLE_NAME,
                    null, wordValues, db.CONFLICT_IGNORE);

            // if row already exists, simply re-query and retrieve id
            if (wordId < 0) {
                String table = DatabaseContract.Words.TABLE_NAME;
                String[] fields = {DatabaseContract.Words._ID};
                String selection =
                        DatabaseContract.Words.COLUMN_WORD + " = ? AND " +
                                DatabaseContract.Words.COLUMN_LANGUAGE + " = ? ";
                String[] selectionArgs = {word, language};

                wordId = db.query(table, fields, selection, selectionArgs,
                        null, null, null)
                        .getLong(0);
            }

            wordIds.put(word, wordId + "");
        }
        db.endTransaction();
    }
}
