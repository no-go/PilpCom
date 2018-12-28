package de.digisocken.pilp_com;

import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NotificationService extends NotificationListenerService {
    public static SharedPreferences pref;

    private String lastPost = "";

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SimpleDateFormat formatOut = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH);

        Notification noti = sbn.getNotification();
        Bundle extras = noti.extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        String pack = sbn.getPackageName();
        String msg = (String) noti.tickerText;
        Object obj = extras.get(Notification.EXTRA_TEXT);
        String msg2 = null;
        Drawable icon = null;

        if (obj != null) {
            msg2 = obj.toString();
        }
        String msg3 = null;
        String msg4 = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            msg3 = extras.getString(Notification.EXTRA_BIG_TEXT);
        }

        try {
            SpannableString sp = (SpannableString) extras.get("android.text");
            if (sp != null) {
                msg4 = sp.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (msg4 != null && msg4.length()>0) msg = msg4;
        if (msg2 != null && msg2.length()>0) msg = msg2;
        if (msg3 != null && msg3.length()>0) msg = msg3;

        String name="NULL";
        try {
            ApplicationInfo appi = this.getPackageManager().getApplicationInfo(pack, 0);
            icon = getPackageManager().getApplicationIcon(appi);
            pack = getPackageManager().getApplicationLabel(appi).toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // catch not normal message .-----------------------------
        if (!sbn.isClearable()) return;
        if (msg == null) return;
        if (msg.equals(lastPost) ) return;

        lastPost  = msg;

        msg = String.format("%s\n[%s]\n%s: %s", formatOut.format(new Date()), pack, title, msg.trim());

        Set<String> set = pref.getStringSet("bonusMsg", new HashSet<String>());
        Set<String> setcopy = new HashSet<>(set);
        setcopy.add(msg);
        pref.edit().putStringSet("bonusMsg", setcopy).commit();
    }
}
