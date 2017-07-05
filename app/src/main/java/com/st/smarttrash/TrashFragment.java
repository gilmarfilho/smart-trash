package com.st.smarttrash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by vinic on 04/07/2017.
 */

public class TrashFragment extends android.support.v4.app.Fragment {
    private static final TrashFragment instance = new TrashFragment();

    private ArrayAdapter<String> trashAdapter;

    public TrashFragment(){
    }

    public static TrashFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] data = {
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        trashAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_trash, // The name of the layout ID.
                        R.id.list_item_trash_textview, // The ID of the textview to populate.
                        weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_trash);
        listView.setAdapter(trashAdapter);



        return rootView;
    }
    public void updateStatus(){
        TrashTask trashTask = new TrashTask(getActivity());
        trashTask.execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        updateStatus();
    }


    private class TrashTask extends AsyncTask<String , Void,  String>  {

        private final String LOG_TAG = TrashTask.class.getSimpleName();

        Context ctx;
        String key;
        SharedPreferences prefs;

        TrashTask(Context ctx){
            this.ctx = ctx;
            prefs = ctx.getSharedPreferences("chave",Context.MODE_PRIVATE);
        }
        private String getSizeFromJson(String sizeJsonStr)
                throws JSONException {


            final String OWM_FEEDS = "feeds";
            final String OWM_FIELD = "field1";
            final String OWM_FIELD2 = "field2";
            final String OWM_FIELD3 = "field3";
            String nivel;
            JSONObject jsonData = new JSONObject(sizeJsonStr);
            JSONArray feeds = jsonData.getJSONArray(OWM_FEEDS);
            JSONObject feedsObj = feeds.getJSONObject(0);
            String fieldValue = feedsObj.getString(OWM_FIELD);
            String field2 = feedsObj.getString(OWM_FIELD2);
            String field3 = feedsObj.getString(OWM_FIELD3);

            if(Integer.parseInt(fieldValue)<700){
                nivel = "BAIXO";
            }
            else if(Integer.parseInt(fieldValue)<900){
                nivel = "MEDIO";
            }
            else nivel = "ALTO";

            String last = "Nível: "+nivel +"\n\n"+ "Local: "+field2 + "\n\n" + "Status: "+field3;


            return last;

        }

        @Override
        public void onPreExecute() {
            key = prefs.getString("chave", "chave");
        }

        protected String doInBackground(String...params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trashJsonStr;

            try {



                // Possible parameters are avaiable at OWM's forecast API page, at
                URL url = new URL("https://api.thingspeak.com/channels/297072/feeds.json?api_key="+key+"&results=1");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trashJsonStr = buffer.toString();
                try{
                return getSizeFromJson(trashJsonStr);
                } catch (JSONException e){
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the speakthing data, there's no point in attemping
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if(result != null){
                trashAdapter.clear();
                trashAdapter.add(result);
            }
        }

    }


}
