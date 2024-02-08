package si.uni_lj.fe.seminar.mojprojekt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private EditText vzdevekEditText;
    private EditText gesloEditText;
    private Button loginButton;
    private TextView registracijaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        vzdevekEditText = findViewById(R.id.vzdevek);
        gesloEditText = findViewById(R.id.geslo);
        loginButton = findViewById(R.id.loginbtn);
        registracijaTextView = findViewById(R.id.registracija);
    }

    private void setClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to handle login logic
                handleLogin();
            }
        });

        registracijaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the registration activity or navigate to the registration screen
                // Example: startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }

    private void handleLogin() {
        // Retrieve values from EditText fields
        String vzdevek = vzdevekEditText.getText().toString();
        String geslo = gesloEditText.getText().toString();

        try {
            String urlBase = getString(R.string.URL_base_storitve);
            String serverUrl = "http://" + urlBase + "/mojProjekt/zaledje/JWT_create.php";
            String params = "upime=" + URLEncoder.encode(vzdevek, "UTF-8") + "&geslo=" + URLEncoder.encode(geslo, "UTF-8");

            new LoginTask(this).execute(serverUrl, params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private static class LoginTask extends AsyncTask<String, Void, String> {
        private WeakReference<LoginActivity> activityReference;

        LoginTask(LoginActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            String serverUrl = params[0];
            String requestParams = params[1];

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(requestParams.getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    inputStream.close();
                    return response.toString();
                } else {
                    return "Error: " + responseCode;
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            LoginActivity activity = activityReference.get();
            if (activity != null) {
                // Call non-static methods using the instance
                activity.processLoginResult(result);
            }
        }
    }

    private void processLoginResult(String result) {
        // Process the result (e.g., parse JSON and store token)
        try {
            JSONObject jsonResult = new JSONObject(result);

            if (jsonResult.has("token")) {
                String jwtToken = jsonResult.getString("token");
                saveTokenToPreferences(jwtToken);

                // Proceed to the main page
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.e("LoginTask", "Authentication failed");
            }
        } catch (JSONException e) {
            Log.e("LoginTask", "Error parsing JSON: " + e.getMessage());
        }
    }

    private void saveTokenToPreferences(String token) {
        // Store the token in SharedPreferences (you might want to use a more secure storage method)
        SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("jwt_token", token);
        editor.apply();
    }
}
