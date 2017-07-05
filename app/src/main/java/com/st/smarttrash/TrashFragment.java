package com.st.smarttrash;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

    private ArrayAdapter<String> trashAdapter;
    private ArrayAdapter<ImageView> imgAdapter;

    public TrashFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String LOG_TAG = TrashFragment.class.getSimpleName();

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
        };

        //final View imgEntryView = inflater.inflate(R.layout.activity_main,null);
        //ImageView img = (ImageView)getActivity().imgEntryView.findViewById( R.id.trash_img);
        //img.setImageResource(R.drawable.empty_trash);

      //  Log.v(LOG_TAG,"ID DA IMAGEM: "+img.getId());
        //Log.v(LOG_TAG,"ID DA IMAGEM 3: "+getActivity().toString());
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        trashAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_trash, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_trash);
        listView.setAdapter(trashAdapter);



        return rootView;
    }
    public void updateStatus(){
        TrashTask trashTask = new TrashTask();
        trashTask.execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        updateStatus();
    }


    private class TrashTask extends AsyncTask<String , Void,  String>  {

        private final String LOG_TAG = TrashTask.class.getSimpleName();

        private String getSizeFromJson(String sizeJsonStr)
                throws JSONException {


            final String OWM_FEEDS = "feeds";
            final String OWM_FIELD = "field1";
            final String OWM_FIELD2 = "field2";
            final String OWM_FIELD3 = "field3";

            JSONObject jsonData = new JSONObject(sizeJsonStr);
            JSONArray feeds = jsonData.getJSONArray(OWM_FEEDS);
            JSONObject feedsObj = feeds.getJSONObject(0);
            String fieldValue = feedsObj.getString(OWM_FIELD);
            String field2 = feedsObj.getString(OWM_FIELD2);
            String field3 = feedsObj.getString(OWM_FIELD3);

            String last = "Volume: "+fieldValue +"\n\n"+ "Localização: "+field2 + "\n\n" + "Status: "+field3;


            return last;

        }

        protected String doInBackground(String...params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("https://api.thingspeak.com/channels/297072/feeds.json?api_key=VSHFMPZG2OY7JCE4&results=1");

                // Create the request to OpenWeatherMap, and open the connection
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
                forecastJsonStr = buffer.toString();
                try{
                return getSizeFromJson(forecastJsonStr);
                } catch (JSONException e){
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
