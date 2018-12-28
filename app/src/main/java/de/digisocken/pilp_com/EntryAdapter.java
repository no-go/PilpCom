package de.digisocken.pilp_com;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EntryAdapter extends BaseAdapter {
    public ArrayList<ContactEntry> theEntries = new ArrayList<>();
    Activity activity;

    EntryAdapter(Activity context) {
        super();
        activity = context;
    }

    public void addItem(ContactEntry item) {
        theEntries.add(item);
    }

    public void clear() {
        theEntries.clear();
    }

    @Override
    public int getCount() {
        return theEntries.size();
    }

    @Override
    public Object getItem(int i) {
        return theEntries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = activity.getLayoutInflater().inflate(R.layout.entry_line, viewGroup, false);
        TextView tt = (TextView) view.findViewById(R.id.line_title);
        TextView tb = (TextView) view.findViewById(R.id.line_body);
        tt.setText(theEntries.get(i).title);
        tb.setText(theEntries.get(i).body);
        if (i%2==0) {
            view.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.colorPrimaryDark
            ));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.colorPrimary
            ));
        }
        return view;
    }

    public void sort() {
        Collections.sort(theEntries, new Comparator<ContactEntry>() {
            @Override
            public int compare(ContactEntry f1, ContactEntry f2) {
                String s1 = f1.title;
                String s2 = f2.title;
                return s1.compareTo(s2);
            }
        });
    }
}