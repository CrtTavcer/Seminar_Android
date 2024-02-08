package si.uni_lj.fe.seminar.mojprojekt;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VnosiActivity extends AppCompatActivity {

    private TextView prikazVnosev;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vsivnosi);

        // Initialize the TextView
        prikazVnosev = findViewById(R.id.prikazVnosev);

        // Call AsyncTask to fetch and display data
        new FetchVnosiTask().execute();
    }

    private class FetchVnosiTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            // Perform the network request in the background
            try {
                String urlBase = getString(R.string.URL_base_storitve);
                URL url = new URL("http://" + urlBase + "/mojProjekt/zaledje/APIji/ledger");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Authorization", "Bearer " + getTokenFromPreferences());

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                // Log the exception for debugging
                Log.e("AsyncTask", "Error during network request", e);
                return null; // handle the error appropriately
            }
        }


        @Override
        protected void onPostExecute(String result) {
            // Process the result and update the UI
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    // Process the data and update your TextView or any other UI components
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            // Replace these keys with the actual keys in your JSON response
                            String vsota = jsonArray.getJSONObject(i).getString("vsota");
                            String vrsta = jsonArray.getJSONObject(i).getString("vrsta");
                            String kategorija = jsonArray.getJSONObject(i).getString("kategorija");
                            String datum = jsonArray.getJSONObject(i).getString("datum");

                            // Add more fields as needed
                            prikazVnosev.append("Vsota: " + vsota + "\n");
                            prikazVnosev.append("Vrsta: " + vrsta + "\n");
                            prikazVnosev.append("Kategorija: " + kategorija + "\n");
                            prikazVnosev.append("Datum: " + datum + "\n\n");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing error for individual items
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing error for the entire array
                }
            } else {
                Toast.makeText(VnosiActivity.this, "Error: No data available", Toast.LENGTH_SHORT).show();
                // Handle the case where the result is null (e.g., network error)
            }
        }
        private String getTokenFromPreferences() {
            SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
            return preferences.getString("jwt_token", "");
        }
    }
}
