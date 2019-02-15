package de.digisocken.pilp_com;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import java.util.Locale;

public class PilpApp extends Application {
    public final static String BROADCAST_EXIT = "de.digisocken.pilp_com.EXIT";
    public static LocationManager locationManager;
    public final static int appleft = 30;
    public final static int apptop = 30;
    public final static int appwidth = 550;

    static public int getPref(String name, SharedPreferences pref, int alternative) {
        try {
            return Integer.parseInt(pref.getString(name, Integer.toString(alternative)));
        } catch (NumberFormatException e) {
            return alternative;
        }
    }

}
