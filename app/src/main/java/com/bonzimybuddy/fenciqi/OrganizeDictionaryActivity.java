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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrganizeDictionaryActivity extends AppCompatActivity
        implements NewDictionaryDialogFragment.NewDictionaryDialogListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private DictionaryRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean mContentPresent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organize_generic_content);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(R.string.organize_dictionary_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow);
        actionBar.setDisplayShowTitleEnabled(true);

        FrameLayout rootLayout = findViewById(R.id.organize_content_container);
        View.inflate(this, R.layout.generic_content_list, rootLayout);
        View.inflate(this, R.layout.no_content, rootLayout);

        TextView noContentWarning = findViewById(R.id.no_content_warning);
        noContentWarning.setText(R.string.no_dictionary_message);

        mRecyclerView = findViewById(R.id.content_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DictionaryRecyclerViewAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onResume() {
        super.onResume();
        enforceContentVisibility();
    }

    // TODO: implement the following with xml bound variables (boolean show)
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
            case R.id.new_dictionary:
                Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                getContentIntent.setType("*/*");
                startActivityForResult(getContentIntent, 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.organize_dictionary_menu, menu);
        return true;
    }

    public void onConfirmDictionaryCreation(DialogFragment dialog) {
        // create and initialize lexicon trie
        Trie lexicon = new Trie();
        NewDictionaryDialogFragment dialogFragment = (NewDictionaryDialogFragment) dialog;
        lexicon.loadWords(dialogFragment.mWords, dialogFragment.mFreqs);

        // dump lexicon to disk
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = timeStamp + ".lex";
        String filesDir = getFilesDir().getAbsolutePath();
        String fullFilePath = filesDir + "/" + fileName;
        if(new File(fullFilePath).exists()) {
            Toast.makeText(this, "Lexicon file already exists on disk", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DiskTrie.dumpToDisk(lexicon, fullFilePath);
        } catch (Exception e) {
            Toast.makeText(this, "Error writing lexicon file to disk", Toast.LENGTH_SHORT).show();
            return;
        }

        // write lexicon record to database
        Dialog dialogView = dialog.getDialog();
        EditText dictionaryNameView = dialogView.findViewById(R.id.new_dictionary_name);
        String dictionaryName = dictionaryNameView.getText().toString();

        ContentValues values = new ContentValues();
        values.put(CatalogContract.Lexica.COLUMN_NAME, dictionaryName);
        values.put(CatalogContract.Lexica.COLUMN_URI, fileName);
        // TODO: language shouldn't be static, also column is incorrect (should be COLUMN_LANGUAGE_ID)
        values.put(CatalogContract.Lexica.COLUMN_LANGUAGE, 1);
        values.put(CatalogContract.Lexica.COLUMN_ENTRIES, dialogFragment.mWords.size());

        Uri newUri = getContentResolver().insert(CatalogContract.Lexica.CONTENT_URI, values);
        if(newUri == null) {
            Toast.makeText(this, "Error adding new dictionary", Toast.LENGTH_SHORT).show();
            return;
        }

/*

        Uri newUri = getContentResolver().insert(CatalogContract.Lexica.CONTENT_URI, values);
        if(newUri == null)
            Toast.makeText(this, "Error adding new dictionary", Toast.LENGTH_SHORT).show();
        else {

            NewDictionaryDialogFragment dialogFragment = (NewDictionaryDialogFragment) dialog;
            ContentValues[] entries = new ContentValues[dialogFragment.mWords.size()];
            for(int i = 0; i < entries.length; i++) {
                entries[i] = new ContentValues();
                entries[i].put(CatalogContract.LexiconWords.COLUMN_WORD, dialogFragment.mWords.get(i));
                entries[i].put(CatalogContract.LexiconWords.COLUMN_FREQ, dialogFragment.mFreqs.get(i));
            }
            int newRows = getContentResolver().bulkInsert(CatalogContract.LexiconWords.buildContentUri(Long.valueOf(newUri.getLastPathSegment())), entries);
        }
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, requestCode, data);

        if(requestCode == 0 && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            Bundle bundle = new Bundle();
            bundle.putParcelable("uri", fileUri);
            DialogFragment newDictionaryDialog = new NewDictionaryDialogFragment();
            newDictionaryDialog.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            newDictionaryDialog.show(fragmentManager, "newDict");
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = CatalogContract.Lexica.CONTENT_URI;
        return new CursorLoader(OrganizeDictionaryActivity.this, uri,
                new String[] {CatalogContract.Lexica._ID, CatalogContract.Lexica.COLUMN_NAME, CatalogContract.Lexica.COLUMN_ENTRIES},
                null, null,
                CatalogContract.Lexica.COLUMN_NAME);
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
