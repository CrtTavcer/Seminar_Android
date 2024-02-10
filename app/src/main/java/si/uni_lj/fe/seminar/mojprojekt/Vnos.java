package si.uni_lj.fe.seminar.mojprojekt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Vnos {
    private Context context; // Context reference for displaying Toast messages

    public Vnos(Context context) {
        this.context = context;
    }

    public void executeHttpPost(String vsota, String vrsta, String namen) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String vsota = params[0];
                String vrsta = params[1];
                String namen = params[2];

                try {
                    // Retrieve JWT token from SharedPreferences
                    SharedPreferences preferences = context.getSharedPreferences("Your_Preference_Name", Context.MODE_PRIVATE);
                    String jwtToken = preferences.getString("jwt_token", "");

                    // Construct URL and open connection
                    String urlBase = context.getString(R.string.URL_base_storitve);
                    URL url = new URL("http://" + urlBase + "/mojProjekt/zaledje/APIji/ledger");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    // Set request properties
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    urlConnection.setDoOutput(true);

                    // Create JSON object with data
                    JSONObject postData = new JSONObject();
                    postData.put("vsota", vsota);
                    postData.put("ID_vrsta", vrsta);
                    postData.put("ID_namen", namen);

                    // Convert JSONObject to byte array
                    byte[] outputBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

                    // Write data to OutputStream
                    OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
                    os.write(outputBytes);
                    os.flush();
                    os.close();

                    // Read response
                    StringBuilder response = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Close connection
                    urlConnection.disconnect();

                    return response.toString();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    // Handle response
                    Toast.makeText(context, "Vnos uspešno dodan", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle error
                    Toast.makeText(context, "Error adding entry", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(vsota, vrsta, namen);
    }
}