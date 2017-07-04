package com.st.smarttrash;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vinic on 04/07/2017.
 */

public class TrashFragment extends android.support.v4.app.Fragment {

    private ArrayAdapter<String> trashAdapter;

    public TrashFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        TrashTask trashTask = new TrashTask();
        trashTask.execute();
    }

    public class TrashTask extends AsyncTask<String , Void,  String[]>  {
        private final String LOG_TAG = TrashTask.class.getSimpleName();
        private String[] getSizeFromJson(String sizeJsonStr)
                throws JSONException {


            final String OWM_FEEDS = "feeds";

            JSONObject jsonData = new JSONObject(sizeJsonStr);
            JSONArray feeds = jsonData.getJSONArray(OWM_FEEDS);

            Log.v(LOG_TAG, "Forecast entry: " + feeds);
            String[] teste = new String[2];
            teste[0] = feeds.toString()+"Forecast entry: ";
            teste[1] = feeds.toString()+"Forecast entry: ";
            return teste;

        }

        protected String[] doInBackground(String...params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String baseUrl = "https://api.thingspeak.com/channels/297072/feeds.json?api_key=VSHFMPZG2OY7JCE4&results=1";
                String apiKey = "&APPID=" + BuildConfig.OPEN_KEY;
                URL url = new URL(baseUrl.concat(apiKey));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
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
    }


}
