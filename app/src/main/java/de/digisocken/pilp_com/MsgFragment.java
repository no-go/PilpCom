package de.digisocken.pilp_com;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class MsgFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mParam1;
    private TextView msgText;
    public static SharedPreferences pref;

    Handler handler = new Handler();
    int delay = 10000; //milliseconds

    public MsgFragment() { }

    public static MsgFragment newInstance(int param1) {
        MsgFragment fragment = new MsgFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plain, container, false);
        msgText = view.findViewById(R.id.section_news);

        handler.postDelayed(new Runnable(){
            public void run(){
                msgText.setText("");
                ArrayList<String> smss = new ArrayList<>();
                readSms(smss,"content://sms/inbox");
                readSms(smss,"content://sms/sent");

                String str = pref.getString("display_limit", "25");
                if (str.equals("")) str = "25";
                int maxim = Integer.parseInt(str);

                Set<String> addi = pref.getStringSet("bonusMsg", new HashSet<String>());
                smss.addAll(addi);

                Collections.sort(smss, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o2.trim().compareTo(o1.trim());
                    }
                });
                for (int i = 0; i < smss.size() && i < maxim; i++) {
                    msgText.append(smss.get(i));
                    msgText.append("\n\n");
                }
                handler.postDelayed(this, delay);
            }
        }, delay);

        ArrayList<String> smss = new ArrayList<>();
        readSms(smss,"content://sms/inbox");
        readSms(smss,"content://sms/sent");

        String str = pref.getString("display_limit", "25");
        if (str.equals("")) str = "25";
        int maxim = Integer.parseInt(str);

        Set<String> addi = pref.getStringSet("bonusMsg", new HashSet<String>());
        smss.addAll(addi);

        Collections.sort(smss, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.trim().compareTo(o1.trim());
            }
        });
        for (int i = 0; i < smss.size() && i < maxim; i++) {
            msgText.append(smss.get(i));
            msgText.append("\n\n");
        }
        return view;
    }

    private void readSms(ArrayList<String> smss, String box) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse(box), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexDate = smsInboxCursor.getColumnIndex("date");
        if (!(indexBody < 0 || !smsInboxCursor.moveToFirst())) {
            do {
                Date date = new Date(smsInboxCursor.getLong(indexDate));
                String line = DateFormat.format(getString(R.string.date_format), date).toString() + "\n";
                line += smsInboxCursor.getString(indexAddress) + "\n";
                line += smsInboxCursor.getString(indexBody);
                smss.add(line);
            } while (smsInboxCursor.moveToNext());
        }
    }
}
