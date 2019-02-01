package com.imperial.quicktexts;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class QuickTextsDialogFragment extends DialogFragment {
    String passedValue;
    Listener mListener;
    Context context;
    ContentResolver resolver;

    String text;
    String textValue;
    String contactName;
    String contactPhone;
    String fullQuickContact;

    public void setStuff(Context context, ContentResolver resolver) {
        this.context = context;
        this.resolver = resolver;
    }

    public void setPassedValue(String passedValue) {
        this.passedValue = passedValue;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void addJSON(String jsonValue);

        void delJSON(String text);

        void addQCJSON(String textValue,
                       String contactName,
                       String contactPhone);

        void delQCJSON(String textValue,
                       String contactName,
                       String contactPhone);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.text_custom_dialog, null);

        if (passedValue == "Add") {
            builder.setView(view)
                    .setTitle("QuickText")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                EditText editText = (EditText) view.findViewById(R.id.dialog_text);
                                String value = editText.getText().toString();
                                mListener.addJSON(value);
                                setPassedValue(value);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        } else if (passedValue == "Delete") {
            final View tempView = inflater.inflate(R.layout.qt_custom_dialog, null);
            Spinner spinner = (Spinner) tempView.findViewById(R.id.spinner);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    text = adapterView.getSelectedItem().toString();
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            TextSpinnerActivity spinnerActivity = new TextSpinnerActivity(context, spinner);
            builder.setView(tempView)
                    .setTitle("Delete QuickText")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListener.delJSON(text);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        } else if (passedValue == "AddQC") {
            final View tempView = inflater.inflate(R.layout.qc_custom_dialog, null);
            Spinner spinner = (Spinner) tempView.findViewById(R.id.spinner);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String result = adapterView.getSelectedItem().toString();
                    int indexOfRes = result.indexOf(':');
                    contactName = result.substring(0, indexOfRes - 1);
                    contactPhone = result.substring(indexOfRes + 2);
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            ContactSpinnerActivity spinnerActivity = new ContactSpinnerActivity(resolver, context, spinner);
            builder.setView(tempView)
                    .setTitle("New QuickContactText")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText editText = (EditText) tempView.findViewById(R.id.qc_text);
                            textValue = editText.getText().toString();
                            mListener.addQCJSON(textValue, contactName, contactPhone);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        } else if (passedValue == "DeleteQC") {
            final View tempView = inflater.inflate(R.layout.qt_custom_dialog, null);
            Spinner spinner = (Spinner) tempView.findViewById(R.id.spinner);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    fullQuickContact = adapterView.getSelectedItem().toString();
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            QCDSpinnerActivity spinnerActivity = new QCDSpinnerActivity(context, spinner);
            builder.setView(tempView)
                    .setTitle("Delete QuickContactText")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (fullQuickContact == null) {
                                text = "";
                                contactName = "";
                                contactPhone = "";
                            } else {
                                int tI, nI;
                                tI = fullQuickContact.indexOf(" ::: ");
                                text = fullQuickContact.substring(0, tI);
                                fullQuickContact = fullQuickContact.substring(tI + 5);
                                nI = fullQuickContact.indexOf(" ::: ");
                                contactName = fullQuickContact.substring(0, nI);
                                fullQuickContact = fullQuickContact.substring(nI + 5);
                                contactPhone = fullQuickContact;
                                mListener.delQCJSON(text, contactName, contactPhone);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        }


        return builder.create();
    }
}
