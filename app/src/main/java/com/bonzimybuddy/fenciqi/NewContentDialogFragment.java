package com.bonzimybuddy.fenciqi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class NewContentDialogFragment extends DialogFragment {
    public interface NewContentDialogListener {
        void onConfirmContentCreation(DialogFragment dialog);
    }

    public ArrayList<String> mWords = new ArrayList<String>();
    public ArrayList<Integer> mFreqs = new ArrayList<Integer>();

    private NewContentDialogListener mListener;
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
                int freq;
                while((line = buf.readLine()) != null) {
                    parts = line.split("\\s+", 1);
                    if(parts.length == 1 || parts.length > 2) {
                        mWords.add(parts[0]);
                        mFreqs.add(0);
                    } else if (parts.length == 2) {
                        try {
                            freq = Integer.valueOf(parts[1]);
                        } catch (NumberFormatException e) {
                            freq = 0;
                        }
                        mWords.add(parts[0]);
                        mFreqs.add(freq);
                    }
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
        mListener = (NewContentDialogListener) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.new_dictionary_dialog, null);
        TextView entryCountView = contentView.findViewById(R.id.new_dictionary_count);
        entryCountView.setText(String.valueOf(mWords.size()));

        builder.setView(contentView)
                .setTitle(R.string.new_dictionary_dialog_title)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirmContentCreation(NewContentDialogFragment.this);
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
