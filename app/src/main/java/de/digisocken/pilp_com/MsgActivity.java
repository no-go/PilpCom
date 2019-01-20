package de.digisocken.pilp_com;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class MsgActivity extends AppCompatActivity {
    private NotificationReceiver nReceiver;
    private TextView msgText;
    private ScrollView scrollView;
    public static SharedPreferences pref;

    Handler handler = new Handler();
    int delay = 10000; //milliseconds

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PilpApp.BROADCAST_EXIT);
        registerReceiver(nReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(getString(R.string.BROADCASTMSG));
        registerReceiver(receiver, filter2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String readMessage = intent.getStringExtra("data");
            new Thread() {
                public void run() {
                    Instrumentation instr = new Instrumentation();
                    if (readMessage.startsWith("k")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_K);
                    if (readMessage.startsWith("i")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_I);
                    //if (readMessage.startsWith("j")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_J);
                    if (readMessage.startsWith("m")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_M);
                    if (readMessage.startsWith("o")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_O);

                    if (readMessage.startsWith("u")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                    if (readMessage.startsWith("d")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                    if (readMessage.startsWith("l")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
                    if (readMessage.startsWith("r")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
                    if (readMessage.startsWith("t")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
                    if (readMessage.startsWith("s")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                }
            }.start();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nReceiver = new NotificationReceiver();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.fragment_msg);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        msgText = findViewById(R.id.section_news);
        scrollView = findViewById(R.id.scrollViewMsg);

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

        // ------------------------------------------------layoutsize

        int left = PilpApp.getPref("appleft", pref, PilpApp.appleft);
        int top = PilpApp.getPref("apptop", pref, PilpApp.apptop);
        int width = PilpApp.getPref("appwidth", pref, PilpApp.appwidth);

        LinearLayout re = findViewById(R.id.block_msg_main);
        re.setPadding(left, top, re.getPaddingRight(), re.getPaddingBottom());

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout ll1 = findViewById(R.id.thetabs);
        ll1.setLayoutParams(lp1);

        scrollView.setLayoutParams(lp1);




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
    }

    private void readSms(ArrayList<String> smss, String box) {
        ContentResolver contentResolver = getContentResolver();
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                toArea(null);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                toWho(null);
                return true;

            case KeyEvent.KEYCODE_K:
                toClk(null);
                return true;
            case KeyEvent.KEYCODE_I:
                toWho(null);
                return true;
            case KeyEvent.KEYCODE_M:
                toArea(null);
                return true;
            case KeyEvent.KEYCODE_O:
                toNews(null);
                return true;

            case KeyEvent.KEYCODE_PAGE_UP:
                int y = scrollView.getScrollY()-100;
                if (y<0) y=0;
                scrollView.smoothScrollTo(0, y);
                return true;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                scrollView.smoothScrollTo(0, scrollView.getScrollY()+100);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void toClk(View view) {
        Intent intent = new Intent(this, ClockActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void toWho(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void toArea(View view) {
        Intent intent = new Intent(this, AreaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void toNews(View view) {
        Intent intent = new Intent(this, NewsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getLocalClassName(), "broadcast");
            if (intent.getBooleanExtra("EXIT", false)) finishAffinity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
