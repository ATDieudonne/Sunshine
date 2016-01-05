package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> mForecastAdapter;
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //create ArrayList that will populate the listview
        List<String> weekForecast = new ArrayList<String>();
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
        mForecastAdapter = new ArrayAdapter<String>
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

        //Create listener for clicked listview item
        my_listview_forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Place forecast data from clicked view by calling the getItem
                //method on mForecastAdapter
                String forecast = mForecastAdapter.getItem(position);
                //Create intent to open DetailActivity
                Intent detailsIntent = new Intent(getActivity(),DetailActivity.class);
                detailsIntent.putExtra(detailsIntent.EXTRA_TEXT,forecast);
                startActivity(detailsIntent);
            }
        });

        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        //create string to be used as tag for application
        public final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        //Going to insert networking code snippet from Udacity's GitHub repo

        /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            //For log verification to be removed in final product.
            /*
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            */
            return resultStrs;

        }

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        protected String[] doInBackground(String... params) {
            //If there is no parameters then there is nothing to fetch
            if (params.length == 0){
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            //set variables that will be used for building URL
            String format = "json";
            String units = "metric";
            int numDays = 7;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&appid=c61efdd3ab7bffa80970c5c145abbb6b");

                //The following strings will be used to build the URL
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                //Time to build the URL
                //Declare a Uri object and run the parse command
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                              .appendQueryParameter(QUERY_PARAM, params[0])//params[0] will return the first string passes to the FetchWeatherTask execute command
                              .appendQueryParameter(FORMAT_PARAM, format)
                              .appendQueryParameter(UNITS_PARAM, units)
                              .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                              .appendQueryParameter(APPID_PARAM,getString(R.string.open_weather_map_app_id))
                        .build();

                //Place the build URL string into URL variable for future use
                URL url = new URL(builtUri.toString());
                //Create a verbose (annotated by the "v" printing the built URL. To be removved in final code
               // Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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

                //Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);



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
            //End of Udacity Code
            try {
                return getWeatherDataFromJson(forecastJsonStr,numDays);
            } catch(JSONException e){
                Log.e(LOG_TAG,e.getMessage(),e);
                e.printStackTrace();
            }
            String[] forecast = {forecastJsonStr};
            return forecast;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            mForecastAdapter.clear();
            mForecastAdapter.addAll(strings);
            super.onPostExecute(strings);
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // if there is options menu,
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask= new FetchWeatherTask();
            weatherTask.execute("10034,us");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}