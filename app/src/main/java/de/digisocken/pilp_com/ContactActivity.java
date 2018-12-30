package de.digisocken.pilp_com;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ContactActivity extends AppCompatActivity {
    private NotificationReceiver nReceiver;
    private EntryAdapter entryAdapter;
    private ListView entryList;
    private SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PilpApp.BROADCAST_EXIT);
        registerReceiver(nReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nReceiver = new NotificationReceiver();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.fragment_contact);

        entryAdapter = new EntryAdapter(this);
        entryList = (ListView) findViewById(R.id.contactList);
        entryList.setAdapter(entryAdapter);

        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 final ContactEntry item = (ContactEntry) adapterView.getItemAtPosition(i);

                 LayoutInflater li = LayoutInflater.from(ContactActivity.this);
                 View promptsView = li.inflate(R.layout.contactaccess, null);

                 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                         ContactActivity.this,
                         R.style.AlertDialogCustom
                 );

                 // set prompts.xml to alertdialog builder
                 alertDialogBuilder.setView(promptsView);

                 final EditText userInput = (EditText) promptsView.findViewById(R.id.userInput);
                 ((TextView) promptsView.findViewById(R.id.textView1)).setText(
                         getString(R.string.chat, item.title)
                 );

                 // set dialog message
                 alertDialogBuilder
                         .setCancelable(false)
                         .setPositiveButton(R.string.ok,
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog,int id) {
                                         String msg = userInput.getText().toString();
                                         if (msg.trim().equals("")) {
                                             Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", item.body.trim(), null));
                                             startActivity(intent);
                                         } else {
                                             ArrayList<String> parts = smsManager.divideMessage(msg);
                                             smsManager.sendMultipartTextMessage(item.body, null, parts, null, null);
                                         }
                                     }
                                 })
                         .setNegativeButton(R.string.cancel,
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog,int id) {
                                         dialog.cancel();
                                     }
                                 });
                 AlertDialog alertDialog = alertDialogBuilder.create();
                 alertDialog.show();
             }
        });

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (phoneNumber != null && !phoneNumber.equals("")) {
                ContactEntry ce = new ContactEntry();
                ce.title = name;
                ce.body = phoneNumber;
                entryAdapter.addItem(ce);
            }
        }
        phones.close();

        entryAdapter.sort();
        entryAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_K:
                toClk(null);
                return true;
            case KeyEvent.KEYCODE_J:
                toMsg(null);
                return true;
            case KeyEvent.KEYCODE_M:
                toArea(null);
                return true;
            case KeyEvent.KEYCODE_O:
                toNews(null);
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
    public void toMsg(View view) {
        Intent intent = new Intent(this, MsgActivity.class);
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