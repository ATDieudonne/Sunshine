package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //create ArrayList that will populate the listview
        ArrayList<String> weekForecast = new ArrayList<String>();
        weekForecast.add(0, "Today - Rainy - 57/64");
        weekForecast.add(1, "Tomorrow - Foggy - 54-60");
        weekForecast.add(2, "Friday - Cloudy - 55/65");
        weekForecast.add(3, "Saturday - Partly Cloudy - 59/67");
        weekForecast.add(4, "Sunday - Sunny - 63/69");
        weekForecast.add(5, "Monday - Sunny - 64/70");
        weekForecast.add(6, "Tuesday - Sunny -63/70");
        weekForecast.add(7, "Wednesday - Partly Cloudy - 60/66");
        weekForecast.add(8, "Thursday - Cloudy -50/60");

        //Now that we have the data we can create the adapter that will convert the data into listviews

        //declare new array adapter
        ArrayAdapter mForecastAdapter = new ArrayAdapter<String>
                (
                        //getContext will return the current context (the parent items activity)
                        getContext(),
                        //this will provide the name of the list item layout
                        R.layout.list_item_forecast,
                        //this will provide the name of the textview we create earlier in the item layout above
                        R.id.list_item_forecast_textview,
                        //Finally we include the ArrayList that contains the data we want to populate the listviews with
                        weekForecast);

        //Now to bind the adapter to the list view. But first we'll need to create an reference to the list view as we only created
        //it in the fragment xml file

        ListView my_listview_forecast = (ListView) rootView.findViewById(R.id.listview_forecast);

        //then use the setAdapter method to bind the listview to the adapter
        my_listview_forecast.setAdapter(mForecastAdapter);


        return rootView;
    }


    abstract class FetchWeatherTask extends AsyncTask<HttpURLConnection,Void,Void>{
        //Going to insert networking code snippet from Udacity's GitHub repo

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        protected String doInBackground(HttpURLConnection urlConnection, BufferedReader reader){
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

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
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
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
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        //End of Udacity Code

            return forecastJsonStr;
        }
    }
}