package com.example.assignment2.models;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.assignment2.R;

public class CaptureImageDialogFragment extends DialogFragment {

    public interface NoticeDialogListener {
        public void onPickingOption(int optionIndex);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (NoticeDialogListener) getContext();
        }catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick your option").setItems(R.array.capture_image_options,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPickingOption(which);
                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CaptureImageDialogFragment.this.getDialog().cancel();
            }
        });
        return builder.create();
    }
}
