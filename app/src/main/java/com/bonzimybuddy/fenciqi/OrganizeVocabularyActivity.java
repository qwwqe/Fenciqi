package com.bonzimybuddy.fenciqi;

import android.app.Activity;
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

public class OrganizeVocabularyActivity extends AppCompatActivity
        implements NewVocabularyDialogFragment.NewVocabularyDialogListener,
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

        actionBar.setTitle(R.string.organize_vocabulary_title);
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
            case R.id.new_vocabulary:
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
        inflater.inflate(R.menu.organize_vocabulary_menu, menu);
        return true;
    }

    public void onConfirmVocabularyCreation(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        EditText vocabularyNameView = dialogView.findViewById(R.id.new_vocabulary_name);
        String vocabularyName = vocabularyNameView.getText().toString();

        ContentValues values = new ContentValues();
        values.put(CatalogContract.Vocabularies.COLUMN_NAME, vocabularyName);
        values.put(CatalogContract.Vocabularies.COLUMN_URI, vocabularyName);
        // TODO: language shouldn't be static, also column is incorrect (should be COLUMN_LANGUAGE_ID)
        values.put(CatalogContract.Vocabularies.COLUMN_LANGUAGE, 1);

        Uri newUri = getContentResolver().insert(CatalogContract.Vocabularies.CONTENT_URI, values);
        if(newUri == null)
            Toast.makeText(this, "Error adding new word list", Toast.LENGTH_SHORT).show();
        else {
            NewVocabularyDialogFragment dialogFragment = (NewVocabularyDialogFragment) dialog;
            ContentValues[] entries = new ContentValues[dialogFragment.mWords.size()];
            for(int i = 0; i < entries.length; i++) {
                entries[i] = new ContentValues();
                entries[i].put(CatalogContract.VocabularyWords.COLUMN_WORD, dialogFragment.mWords.get(i));
            }
            // TODO: INCLUDE FREQ IN INSERTION (UNNEEDED? LEXICON TRIE SHOULD CONTAIN FREQS...)
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
        return new CursorLoader(OrganizeVocabularyActivity.this, uri,
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
