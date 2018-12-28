package de.digisocken.pilp_com;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


public class NewsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mParam1;
    private TextView txtNews;
    public static SharedPreferences pref;

    Button clasicBtn;
    Button diamondBtn;
    Button ostBtn;
    Button generalBtn;
    MediaPlayer mediaPlayer;

    private ArrayList<String> title;
    private ArrayList<String> description;

    public NewsFragment() { }

    public static NewsFragment newInstance(int param1) {
        NewsFragment fragment = new NewsFragment();
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
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        txtNews = view.findViewById(R.id.section_news);
        clasicBtn  = view.findViewById(R.id.classicBtn);
        diamondBtn = view.findViewById(R.id.diamondBtn);
        ostBtn = view.findViewById(R.id.ostBtn);
        generalBtn = view.findViewById(R.id.generalBtn);

        RetrieveFeedTask rt = new RetrieveFeedTask();
        rt.execute(pref.getString("rss_url", "https://www.deutschlandfunk.de/die-nachrichten.353.de.rss"));
        try {
            rt.get();
            String dummy = "";
            for (int i=0; i<title.size(); i++) {
                //dummy += "<b>" + title.get(i) + ":</b>\n" + description.get(i) + "<br/>\n<br/>\n";
                dummy += uml(title.get(i)).toUpperCase() + "\n" + uml(description.get(i)) + "\n\n";
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
                pref.getString("ost_url", "http://fallout.fm:8000/falloutfm4.ogg"))
        );
        generalBtn.setOnClickListener(new RadioOnClickListener(
                pref.getString("general_url", "http://fallout.fm:8000/falloutfm10.ogg"))
        );

        return view;
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
            title = new ArrayList<String>();
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
                    title.add(((Node) nameList.item(0)).getNodeValue());

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

            clasicBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            clasicBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            diamondBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            diamondBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            ostBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            ostBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            generalBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
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
            mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(strings[0]));
            mediaPlayer.start();
            return null;
        }
    }
}
