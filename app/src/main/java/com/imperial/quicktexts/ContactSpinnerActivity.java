package com.imperial.quicktexts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;

class ContactSpinnerActivity {

    ContactSpinnerActivity(ContentResolver resolver, final Context context, Spinner spinner) {
        String contactName;
        String phNumber;
        ArrayList<String> contacts = new ArrayList<>();
        Cursor c = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                contactName = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phNumber = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contacts.add(contactName + " : " + phNumber);
            }
            c.close();
        }

        Collections.sort(contacts);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, contacts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

    }

}