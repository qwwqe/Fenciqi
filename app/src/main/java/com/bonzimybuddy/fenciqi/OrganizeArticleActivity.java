package com.bonzimybuddy.fenciqi;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OrganizeArticleActivity extends AppCompatActivity
        implements NewArticleDialogFragment.NewArticleDialogListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private VocabularyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean mContentPresent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organize_generic_content);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(R.string.organize_article_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow);
        actionBar.setDisplayShowTitleEnabled(true);

        FrameLayout rootLayout = findViewById(R.id.organize_content_container);
        View.inflate(this, R.layout.generic_content_list, rootLayout);
        View.inflate(this, R.layout.no_content, rootLayout);

        TextView noContentWarning = findViewById(R.id.no_content_warning);
        noContentWarning.setText(R.string.no_vocabulary_message);

        mRecyclerView = findViewById(R.id.content_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new VocabularyRecyclerViewAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onResume() {
        super.onResume();
        enforceContentVisibility();
    }

    public void enforceContentVisibility() {
        Log.w("visibility", String.valueOf(mContentPresent));
        if(mContentPresent) {
            findViewById(R.id.no_content_warning).setVisibility(View.GONE);
            findViewById(R.id.content_recycler_view).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_content_warning).setVisibility(View.VISIBLE);
            findViewById(R.id.content_recycler_view).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.new_article:
                Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                getContentIntent.setType("*/*");
                startActivityForResult(getContentIntent, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.organize_article_menu, menu);
        return true;
    }

    /**
     * Create article.
     * @param dialog Contains article language, name, and lexicon, as well as full text
     */
    public void onConfirmArticleCreation(DialogFragment dialog) {
        // TODO: abstract article creation
        Dialog dialogView = dialog.getDialog();
        // this EditText should be a drop down
        EditText articleLanguageView = dialogView.findViewById(R.id.new_article_language);
        String articleLanguage = articleLanguageView.getText().toString();

        EditText articleNameView = dialogView.findViewById(R.id.new_article_name);
        String articleName = articleNameView.getText().toString();

        EditText lexiconNameView = dialogView.findViewById(R.id.lexicon_name);
        String lexiconName = lexiconNameView.getText().toString();

        // find lexicon pathname
        Cursor lCursor = getContentResolver().query(CatalogContract.Lexica.CONTENT_URI,
                new String[] {CatalogContract.Lexica.COLUMN_URI },
                CatalogContract.Lexica.COLUMN_NAME + " = ?",
                new String[] {lexiconName}, null);
        String lexiconPath = lCursor.getString(0);
        if(lexiconPath == null) {
            Toast.makeText(this, "Error accessing lexicon", Toast.LENGTH_SHORT).show();
            return;
        }
        lCursor.close();

        // load lexicon trie
        Trie lexiconTrie;
        try {
            // TODO: this will fail?
            lexiconTrie = DiskTrie.loadFromDisk(lexiconPath);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading lexicon from disk", Toast.LENGTH_SHORT).show();
            return;
        }

        // tokenize text
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.loadLexicon(lexiconTrie);
        String corpusText = ((NewArticleDialogFragment) dialog).mCorpusText;
        Corpus article = tokenizer.tokenize(corpusText);
        // TODO: language shouldn't be static
        article.setCorpusName(articleName);
        article.setLanguageName(articleLanguage);
        article.setLanguageId(1 + "");

        ContentValues values = new ContentValues();
        values.put(CatalogContract.Corpora.COLUMN_CORPUS, article.)
        //values.put(CatalogContract.Vocabularies.COLUMN_NAME, vocabularyName);
        //values.put(CatalogContract.Vocabularies.COLUMN_URI, vocabularyName);
        //values.put(CatalogContract.Vocabularies.COLUMN_LANGUAGE, 1);

        //Uri newUri = getContentResolver().insert(CatalogContract.Vocabularies.CONTENT_URI, values);
        if(newUri == null)
            Toast.makeText(this, "Error adding new word list", Toast.LENGTH_SHORT).show();
        else {
            NewArticleDialogFragment dialogFragment = (NewArticleDialogFragment) dialog;
            ContentValues[] entries = new ContentValues[dialogFragment.mWords.size()];
            for(int i = 0; i < entries.length; i++) {
                entries[i] = new ContentValues();
                entries[i].put(CatalogContract.VocabularyWords.COLUMN_WORD, dialogFragment.mWords.get(i));
            }
            int newRows = getContentResolver().bulkInsert(CatalogContract.VocabularyWords.buildContentUri(Long.valueOf(newUri.getLastPathSegment())), entries);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, requestCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            Bundle bundle = new Bundle();
            bundle.putParcelable("uri", fileUri);
            DialogFragment newVocabularyDialog = new NewVocabularyDialogFragment();
            newVocabularyDialog.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            newVocabularyDialog.show(fragmentManager, "newVocab");
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = CatalogContract.Vocabularies.CONTENT_URI;
        return new CursorLoader(OrganizeArticleActivity.this, uri,
                new String[] {CatalogContract.Vocabularies._ID, CatalogContract.Vocabularies.COLUMN_NAME, CatalogContract.Vocabularies.COLUMN_COUNT},
                null, null,
                CatalogContract.Vocabularies.COLUMN_NAME);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        if(data != null && data.getCount() > 0)
            mContentPresent = true;
        else
            mContentPresent = false;
        enforceContentVisibility();
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
