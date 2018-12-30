package de.digisocken.pilp_com;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class NewsActivity extends AppCompatActivity {
    private NotificationReceiver nReceiver;
    private TextView txtNews;
    public static SharedPreferences pref;

    Button clasicBtn;
    Button diamondBtn;
    Button ostBtn;
    Button generalBtn;
    MediaPlayer mediaPlayer;

    private ArrayList<String> titles;
    private ArrayList<String> description;
    private ScrollView scrollView;

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

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.fragment_news);

        txtNews = findViewById(R.id.section_news);
        clasicBtn  = findViewById(R.id.classicBtn);
        diamondBtn = findViewById(R.id.diamondBtn);
        ostBtn = findViewById(R.id.ostBtn);
        generalBtn = findViewById(R.id.generalBtn);
        scrollView = findViewById(R.id.scrollViewNews);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        RetrieveFeedTask rt = new RetrieveFeedTask();
        rt.execute(pref.getString("rss_url", "https://www.deutschlandfunk.de/die-nachrichten.353.de.rss"));
        try {
            rt.get();
            String dummy = "";
            for (int i=0; i<titles.size(); i++) {
                //dummy += "<b>" + title.get(i) + ":</b>\n" + description.get(i) + "<br/>\n<br/>\n";
                dummy += uml(titles.get(i)).toUpperCase() + "\n" + uml(description.get(i)) + "\n\n";
            }
            txtNews.setText(dummy);
            //txtNews.setText(Html.fromHtml(dummy));
        } catch (Exception e) {
            e.printStackTrace();
        }

        clasicBtn.setOnClickListener(new RadioOnClickListener(
                pref.getString("classic_url", "http://fallout.fm:8000/falloutfm7.ogg"))
        );
        diamondBtn.setOnClickListener(new RadioOnClickListener(
                pref.getString("diamond_url", "http://fallout.fm:8000/falloutfm6.ogg"))
        );
        ostBtn.setOnClickListener(new RadioOnClickListener(
                pref.getString("ost_url", "http://wackenradio-high.rautemusik.fm"))
        );
        generalBtn.setOnClickListener(new RadioOnClickListener(
                pref.getString("general_url", "http://fallout.fm:8000/falloutfm10.ogg"))
        );

    }

    private String uml(String str) {
        str = str.replace("ß", "ss");
        str = str.replace("ä", "ae");
        str = str.replace("ö", "oe");
        str = str.replace("ü", "ue");
        str = str.replace("Ä", "Ae");
        str = str.replace("Ö", "Oe");
        str = str.replace("Ü", "Ue");
        return  str;
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urlsStr) {
            titles = new ArrayList<String>();
            description = new ArrayList<String>();

            try {
                URL url = new URL(urlsStr[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("item");
                for (int i = 0; i < nodeList.getLength(); i++) {

                    Node node = nodeList.item(i);

                    Element fstElmnt = (Element) node;
                    NodeList nameList = fstElmnt.getElementsByTagName("title");
                    Element nameElement = (Element) nameList.item(0);
                    nameList = nameElement.getChildNodes();
                    titles.add(((Node) nameList.item(0)).getNodeValue());

                    NodeList websiteList = fstElmnt.getElementsByTagName("description");
                    Element websiteElement = (Element) websiteList.item(0);
                    websiteList = websiteElement.getChildNodes();
                    description.add(
                            Html.fromHtml(
                                    ((Node) websiteList.item(0)).getNodeValue()
                            ).toString()
                    );
                }
            } catch (Exception e) {
                System.out.println("XML Pasing Excpetion = " + e);
            }
            return "ok";
        }
    }

    private class RadioOnClickListener implements View.OnClickListener {
        private String _station;

        public RadioOnClickListener(String station) {
            _station = station;
        }

        @Override
        public void onClick(View view) {
            Button btn = (Button) view;

            clasicBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarker));
            clasicBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            diamondBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarker));
            diamondBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            ostBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarker));
            ostBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            generalBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarker));
            generalBtn.setTextColor(getResources().getColor(R.color.colorAccent));

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                new PlayTask().execute(_station);
            }
        }
    }

    private class PlayTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(strings[0]));
            mediaPlayer.start();
            return null;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_K:
                toClk(null);
                return true;
            case KeyEvent.KEYCODE_I:
                toWho(null);
                return true;
            case KeyEvent.KEYCODE_J:
                toMsg(null);
                return true;
            case KeyEvent.KEYCODE_M:
                toArea(null);
                return true;

            case KeyEvent.KEYCODE_1:
                clasicBtn.callOnClick();
                return true;
            case KeyEvent.KEYCODE_2:
                diamondBtn.callOnClick();
                return true;
            case KeyEvent.KEYCODE_3:
                ostBtn.callOnClick();
                return true;
            case KeyEvent.KEYCODE_4:
                generalBtn.callOnClick();
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