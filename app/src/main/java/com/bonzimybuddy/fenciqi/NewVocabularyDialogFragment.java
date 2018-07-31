package com.bonzimybuddy.fenciqi;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class NewVocabularyDialogFragment extends DialogFragment {
    public interface NewVocabularyDialogListener {
        public void onConfirmVocabularyCreation(DialogFragment dialog);
    }

    public ArrayList<String> mWords = new ArrayList<String>();
    public ArrayList<String> mDates = new ArrayList<String>();

    private NewVocabularyDialogListener mListener;
    private Uri mUri;
    private Context mContext;
    private InputStream mInputStream;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUri = getArguments().getParcelable("uri");
            mContext = getContext();
            // TODO: move this elsewhere (implement as callback?)
            try {
                mInputStream = mContext.getContentResolver().openInputStream(mUri);
                BufferedReader buf = new BufferedReader(new InputStreamReader(mInputStream));
                String line;
                String parts[];
                while((line = buf.readLine()) != null) {
                    parts = line.split("\\s+", 2);
                    if(parts.length >= 1)
                        mWords.add(parts[0]);
                }

            } catch (IOException e) {
                Toast.makeText(mContext, "Error processing file", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (NewVocabularyDialogListener) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.new_vocabulary_dialog, null);
        TextView entryCountView = contentView.findViewById(R.id.new_vocabulary_count);
        entryCountView.setText(String.valueOf(mWords.size()));

        builder.setView(contentView)
                .setTitle(R.string.new_vocabulary_dialog_title)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirmVocabularyCreation(NewVocabularyDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }
}
