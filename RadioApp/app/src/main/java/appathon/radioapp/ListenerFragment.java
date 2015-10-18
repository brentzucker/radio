package appathon.radioapp;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.squareup.okhttp.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * <p/>
 * Use the {@link ListenerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListenerFragment extends Fragment {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button addSong;
    private String song_id;
    private EditText inputText;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListenerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListenerFragment newInstance(String param1, String param2) {
        ListenerFragment fragment = new ListenerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ListenerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_listener, container, false);
        inputText = (EditText) rootView.findViewById(R.id.inputID);
        addSong = (Button) rootView.findViewById(R.id.addSong);
        addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "click",Toast.LENGTH_LONG).show();
                //Log.i("&&&&&&&&&", "button pressed");
                song_id = (inputText.getText()).toString();
                Log.i("entered string", song_id);
                new BackgroundTaskButton().execute();
            }
        });
        new BackgroundTask().execute();
        return rootView;
    }


    public class BackgroundTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String client_id;
            String token;
            String songId;
            String json = "";
            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url("http://104.236.76.46:8080/api/getCurrentSong.json")
                        .build();
                Response response = client.newCall(request).execute();
                json = response.body().string();
                JSONObject jObj = new JSONObject(json);
                songId = jObj.getString("song_id");
//              Scanner readFile = new Scanner(new File("key.txt"));
                InputStream is = getActivity().getAssets().open("key.txt");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                String file = new String(buffer);
                String line = file.split("\n")[0]; //readFile.nextLine();
                client_id = line;
                line = file.split("\n")[1];//readFile.nextLine();
                token = line;
                String url = "https://api.soundcloud"
                        + ".com/tracks/" + songId + "/download?client_id=" + client_id + "&oauth_token=" + token;
                //Log.i("&&&&&&&&&", url);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(url);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                        }
                        mp.seekTo(49000);
                    }
                });
                mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
            } catch (Exception e) {
                Log.e("&&&&&&&", e.getLocalizedMessage());
            }
            return json;
        }
    }

    public class BackgroundTaskButton extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            /* Read in Soundcloud API Key */
            String key = "";
            try {
                InputStream is = getActivity().getAssets().open("key.txt");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                String file = new String(buffer);
                String line = file.split("\n")[0]; //readFile.nextLine();
                key = line;
            } catch (IOException e) {
            }

            /* Get Query from Text Box */
            String query = "atlanta";

            /* Query Soundcloud: Get Id of Top Result */

            String soundcloud_query_url = "http://api.soundcloud.com/tracks.json?client_id=" + key + "&q=" + query + "&limit=1";
            String song_id = "0";
            String json = "";
            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url(soundcloud_query_url)
                        .build();
                Response response = client.newCall(request).execute();
                json = response.body().string();
                json = json.substring(1, json.length() - 1);
                Log.i("json", json);
                JSONObject jObj = new JSONObject(json);
                song_id = jObj.getString("id");
            } catch (Exception e) {
                Log.e("Bad SoundCloud Request", e.getLocalizedMessage());
            }

            Log.i("song_id", "" + song_id);

            /* Post Song Id to Queue */

            client = new OkHttpClient();
            json = "{\"song_id\" : " + song_id + "}";
            Log.i("&&&&&&&&&", "button pressed");
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("http://104.236.76.46:8080/api/addSong")
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String r = response.body().string();
            } catch (Exception e) {
            }
            return null;
        }
    }
}

