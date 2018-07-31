package com.bonzimybuddy.fenciqi;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class OrganizeDocumentsActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organize_documents);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();

        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow);
        actionbar.setDisplayShowTitleEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        CardView organizeArticleCard = (CardView) findViewById(R.id.organize_article_card);
        CardView organizeVocabularyCard = (CardView) findViewById(R.id.organize_vocabulary_card);
        CardView organizeDictionaryCard = (CardView) findViewById(R.id.organize_dictionary_card);

        // update article stats
        // TODO: THIS SHOULDN'T BE IN onCreate()
        // ...

        // update lexica stats
        // TODO: THIS SHOULDN'T BE IN onCreate()

        Cursor lCursor = getContentResolver().query(CatalogContract.Lexica.CONTENT_URI,
                new String[] {CatalogContract.Lexica._ID, CatalogContract.Lexica.COLUMN_ENTRIES},
                null, null, null);
        int dictionaryCount = lCursor.getCount();
        int dictionaryEntries = 0;
        while(lCursor.moveToNext()) {
            dictionaryEntries += lCursor.getInt(1);
        }
        lCursor.close();

        TextView dictionaryCountTextView = findViewById(R.id.dictionaries_total);
        dictionaryCountTextView.setText(String.valueOf(dictionaryCount) + "冊");
        TextView dictionaryEntriesTextView = findViewById(R.id.dictionary_entries_total);
        dictionaryEntriesTextView.setText(String.valueOf(dictionaryEntries) + "詞");

        // update vocabulary stats
        // TODO: THIS ALSO SHOULDN'T BE IN onCreate()
        Cursor vCursor = getContentResolver().query(CatalogContract.Vocabularies.CONTENT_URI,
                new String[] {CatalogContract.Vocabularies._ID, CatalogContract.Vocabularies.COLUMN_COUNT},
                null, null, null);
        int wordlistCount = vCursor.getCount();
        int wordlistEntries = 0;
        while(vCursor.moveToNext()) {
            wordlistEntries += vCursor.getInt(1);
        }
        vCursor.close();

        TextView wordlistCountTextView = findViewById(R.id.vocabularies_total);
        wordlistCountTextView.setText(String.valueOf(wordlistCount) + "冊");
        TextView wordlistEntriesTextView = findViewById(R.id.vocabulary_entries_total);
        wordlistEntriesTextView.setText(String.valueOf(wordlistEntries) + "詞");

        // init listeners
        organizeArticleCard.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(OrganizeDocumentsActivity.this,
                       OrganizeArticleActivity.this);
               startActivity(intent);
           }
        });

        organizeVocabularyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizeDocumentsActivity.this,
                        OrganizeVocabularyActivity.class);
                startActivity(intent);
            }
        });

        organizeDictionaryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizeDocumentsActivity.this,
                        OrganizeDictionaryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
