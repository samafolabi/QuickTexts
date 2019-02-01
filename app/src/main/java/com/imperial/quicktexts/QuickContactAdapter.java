package com.imperial.quicktexts;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class QuickContactAdapter extends ArrayAdapter<QuickContact> {

    private Context context;
    private int resource;
    private QuickContact objects[] = null;

    QuickContactAdapter(Context context, int resource, QuickContact[] objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        QuickContactHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(resource, parent, false);

            holder = new QuickContactHolder();
            holder.textTextView = (TextView) row.findViewById(R.id.text_message);
            holder.contactTextView = (TextView) row.findViewById(R.id.contact_name_number);

            row.setTag(holder);
        } else {
            holder = (QuickContactHolder) row.getTag();
        }

        QuickContact quickContact = objects[position];
        holder.textTextView.setText(quickContact.text);
        holder.contactTextView.setText(quickContact.contact);

        return row;

    }

    private static class QuickContactHolder {
        TextView textTextView;
        TextView contactTextView;
    }
}
