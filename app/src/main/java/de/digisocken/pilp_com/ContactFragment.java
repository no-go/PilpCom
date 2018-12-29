package de.digisocken.pilp_com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


public class ContactFragment extends Fragment {

    private EntryAdapter entryAdapter;
    private ListView entryList;
    private SmsManager smsManager = SmsManager.getDefault();

    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static ContactFragment newInstance(int page, String title) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactFragment() { }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        entryAdapter = new EntryAdapter(getActivity());
        entryList = (ListView) view.findViewById(R.id.contactList);
        entryList.setAdapter(entryAdapter);
        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 final ContactEntry item = (ContactEntry) adapterView.getItemAtPosition(i);

                 LayoutInflater li = LayoutInflater.from(getActivity());
                 View promptsView = li.inflate(R.layout.contactaccess, null);

                 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);

                 // set prompts.xml to alertdialog builder
                 alertDialogBuilder.setView(promptsView);

                 final EditText userInput = (EditText) promptsView.findViewById(R.id.userInput);

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
        Cursor phones = container.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
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

        return view;
    }
}
