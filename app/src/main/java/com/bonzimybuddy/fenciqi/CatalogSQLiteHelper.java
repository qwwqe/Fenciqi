package com.bonzimybuddy.fenciqi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

public class CatalogSQLiteHelper extends SQLiteOpenHelper {
    private static CatalogSQLiteHelper sInstance;

    public static synchronized CatalogSQLiteHelper getInstance(Context context, String dbName,
                                                               Cursor cursor, Integer dbVersion) {
        if (sInstance == null) {
            sInstance = new CatalogSQLiteHelper(context.getApplicationContext(), dbName, cursor, dbVersion);
        }
        return sInstance;
    }

    private CatalogSQLiteHelper(Context context, String dbName, Cursor cursor, Integer dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (String statement: DatabaseContract.CREATE_TABLE_STATEMENTS) {
            sqLiteDatabase.execSQL(statement);
        }

        for (String statement: DatabaseContract.CREATE_VIEW_STATEMENTS) {
            sqLiteDatabase.execSQL(statement);
        }

        // TODO: abstract this
        sqLiteDatabase.execSQL("INSERT INTO " + DatabaseContract.Languages.TABLE_NAME +
                " (" + DatabaseContract.Languages._ID + ", " +
                DatabaseContract.Languages.COLUMN_NAME +
                ") VALUES (1, \"zh-tw\");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // dummy
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}
