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

        List<String> trashData = new ArrayList<String>(Arrays.asList(data));
        trashAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_trash, // The name of the layout ID.
                        R.id.list_item_trash_textview, // The ID of the textview to populate.
                        trashData);

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


    private class TrashTask extends AsyncTask<String , Void,  String[]>  {

        private final String LOG_TAG = TrashTask.class.getSimpleName();

        Context ctx;
        String key;
        SharedPreferences prefs;

        TrashTask(Context ctx){
            this.ctx = ctx;
            prefs = ctx.getSharedPreferences("chave",Context.MODE_PRIVATE);
        }

        private String[] getDataFromJson(String dataJsonStr)
                throws JSONException {


            final String OWM_FEEDS = "feeds";
            String [] OWM_FIELD = new String[8];
            for(int i = 0;i<OWM_FIELD.length;i++){
                OWM_FIELD[i]="field"+(i+1);
            }

            String nivel;
            JSONObject jsonData = new JSONObject(dataJsonStr);
            JSONArray feeds = jsonData.getJSONArray(OWM_FEEDS);

            JSONObject feedsObj = feeds.getJSONObject(0);
            String [] fieldValue = new String[8];
            for(int i = 0;i<fieldValue.length;i++){
               fieldValue[i]=feedsObj.getString(OWM_FIELD[i]);
            }
            String [] last = new String[4];

            if(Integer.parseInt(fieldValue[0])<200){
                nivel = "BAIXO";
            }
            else if(Integer.parseInt(fieldValue[0])<350){
                nivel = "MEDIO";
            }
            else nivel = "ALTO";

            last[0] = "Nível: "+nivel +"\n\n"+ "Local: "+fieldValue[1];

            if(Integer.parseInt(fieldValue[2])<200){
                nivel = "BAIXO";
            }
            else if(Integer.parseInt(fieldValue[2])<350){
                nivel = "MEDIO";
            }
            else nivel = "ALTO";

            last[1] = "Nível: "+nivel +"\n\n"+ "Local: "+fieldValue[3];

            if(Integer.parseInt(fieldValue[4])<200){
                nivel = "BAIXO";
            }
            else if(Integer.parseInt(fieldValue[4])<350){
                nivel = "MEDIO";
            }
            else nivel = "ALTO";

            last[2] = "Nível: "+nivel +"\n\n"+ "Local: "+fieldValue[5];

            if(Integer.parseInt(fieldValue[6])<200){
                nivel = "BAIXO";
            }
            else if(Integer.parseInt(fieldValue[6])<350){
                nivel = "MEDIO";
            }
            else nivel = "ALTO";

            last[3] = "Nível: "+nivel +"\n\n"+ "Local: "+fieldValue[7];

            return last;

        }

        @Override
        public void onPreExecute() {
            key = prefs.getString("chave", "chave");
        }

        protected String[] doInBackground(String...params) {
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
                return getDataFromJson(trashJsonStr);
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
        protected void onPostExecute(String result[]) {
            trashAdapter.clear();
            if(result != null){
                for(String trashStr: result){
                    trashAdapter.add(trashStr);
                }
            }
        }

    }


}
