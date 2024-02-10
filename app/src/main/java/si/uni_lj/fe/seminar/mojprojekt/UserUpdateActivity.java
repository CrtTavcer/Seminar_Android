package si.uni_lj.fe.seminar.mojprojekt;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserUpdateActivity extends AppCompatActivity {
    private EditText editime;
    private EditText editoriimek;
    private EditText editgeslo;
    private EditText editemail;
    private String urlBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        // Initialize EditText fields
        editime = findViewById(R.id.editime);
        editoriimek = findViewById(R.id.editoriimek);
        editgeslo = findViewById(R.id.editgeslo);
        editemail = findViewById(R.id.editemail);

        // Initialize urlBase
        urlBase = getString(R.string.URL_base_storitve);

        //za polnjenje polj uporabnika
        getUserData();
        // Call updateUser when the button is clicked
        findViewById(R.id.DataBtn).setOnClickListener(v -> updateUser());
    }
    private void updateUser() {
        // Get the data from EditText fields
        String ime = editime.getText().toString();
        String priimek = editoriimek.getText().toString();
        String geslo = editgeslo.getText().toString();
        String email = editemail.getText().toString();

        // Check if any of the EditText fields are empty
        if (ime.isEmpty() || priimek.isEmpty() || geslo.isEmpty() || email.isEmpty()) {
            Toast.makeText(UserUpdateActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return; // Exit the method if any field is empty
        }

        // Create a JSON object to hold the data
        JSONObject postData = new JSONObject();
        try {
            postData.put("ime", ime);
            postData.put("priimek", priimek);
            postData.put("geslo", geslo);
            postData.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Execute AsyncTask to perform the PUT request
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    // Create URL for the API endpoint

                    URL url = new URL("http://"+urlBase+"/mojProjekt/zaledje/APIji/uporabniki/vzdevek");
                    SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
                    String jwtToken = preferences.getString("jwt_token", "");

                    // Open connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    urlConnection.setDoOutput(true);

                    // Write data to the connection
                    OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    outputStream.write(postData.toString().getBytes());
                    outputStream.flush();

                    // Get response code
                    int responseCode = urlConnection.getResponseCode();
                    Log.d("Response Code", String.valueOf(responseCode));

                    // Close connections
                    outputStream.close();
                    urlConnection.disconnect();

                    // Check if the request was successful
                    return responseCode == HttpURLConnection.HTTP_OK;

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    // Request was successful
                    Toast.makeText(UserUpdateActivity.this, "User data updated successfully", Toast.LENGTH_SHORT).show();

                    // Clear EditText fields
                    editime.setText("");
                    editoriimek.setText("");
                    editgeslo.setText("");
                    editemail.setText("");
                } else {
                    // Request failed
                    Toast.makeText(UserUpdateActivity.this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                }
            }

        }.execute();
    }

    private void getUserData() { //klicanje APIja za vračanje podatkov uporabnika. te nato vrinemo v vnosna polja
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    // Create URL for the API endpoint
                    String url = "http://"+urlBase+"/mojProjekt/zaledje/APIji/uporabniki/vzdevek";
                    SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
                    String jwtToken = preferences.getString("jwt_token", "");
                    // Open connection
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + jwtToken);

                    // Get response code
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read response data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Parse JSON response
                        return new JSONObject(response.toString());
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject userData) {
                if (userData != null) {
                    // Process user data
                    try {
                        String ime = userData.getString("ime");
                        String priimek = userData.getString("priimek");
                        String email = userData.getString("email");
                        editime.setText(ime);
                        editoriimek.setText(priimek);
                        editemail.setText(email);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Failed to get user data
                    Toast.makeText(UserUpdateActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


}
