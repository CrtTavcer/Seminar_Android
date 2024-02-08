package si.uni_lj.fe.seminar.mojprojekt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // Constants for menu items
    private final int MENU_POSODOBI_PODATKE = 0;
    private final int MENU_VNOSI = 1;
    private final int MENU_JWT = 2;
    private final int MENU_IZHOD = -1;

    private TableLayout lastInputsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a JWT token is stored
        if (!isTokenStored()) {
            // Token not found, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // Finish MainActivity so that the user can't go back to it using the back button
            return; // Exit the method to avoid further execution if the token is not stored
        }

        setContentView(R.layout.activity_main);
        init();

        Button addExpenseButton = findViewById(R.id.addExpenseButton);
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddExpenseDialog();
            }
        });
    }

    private void init() {
        lastInputsTable = findViewById(R.id.lastInputsTable);
        zadnjiVnosi();
    }

    private void showAddExpenseDialog() { //odpiranje okna za dodajanje novega vnosa
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dialog, null);
        builder.setView(dialogView);

        EditText editTextVsota = dialogView.findViewById(R.id.editTextVsota);
        EditText editTextVrsta = dialogView.findViewById(R.id.editTextVrsta);
        EditText editTextNamen = dialogView.findViewById(R.id.editTextNamen);

        builder.setPositiveButton("Potrdi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the confirmation button click here
                String vsota = editTextVsota.getText().toString();
                String vrsta = editTextVrsta.getText().toString();
                String namen = editTextNamen.getText().toString();
                // Do something with the input values
                Vnos vnos = new Vnos(MainActivity.this); //klicanje objekta iz Vnos.java
                // Execute HTTP POST request
                vnos.executeHttpPost(vsota, vrsta, namen);
                zadnjiVnosi();
            }
        });

        builder.setNegativeButton("Prekliči", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel button clicked, do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void zadnjiVnosi() {
        new AsyncTask<Void, Void, String>() {
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
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                // Process the result and update the UI
                if (result != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(result);

                        // Clear existing rows from the table
                        lastInputsTable.removeAllViews();

                        // Process the data and update your TableLayout
                        int length = jsonArray.length();
                        int endIndex = Math.min(10, length); // End index for the loop
                        for (int i = 0; i < endIndex; i++) {
                            try {
                                // Replace these keys with the actual keys in your JSON response
                                String vsota = jsonArray.getJSONObject(i).getString("vsota");
                                String vrsta = jsonArray.getJSONObject(i).getString("vrsta");

                                // Create a new row and add it to the table
                                TableRow row = new TableRow(MainActivity.this);

                                // Create TextViews for displaying vsota and vrsta
                                TextView vsotaTextView = new TextView(MainActivity.this);
                                vsotaTextView.setText(vsota);
                                vsotaTextView.setPadding(16, 8, 16, 8); // Adjust padding as needed
                                vsotaTextView.setTextSize(18); // Set font size
                                vsotaTextView.setTextColor(Color.BLACK); // Set text color to black
                                vsotaTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1)); // Align to left

                                TextView vrstaTextView = new TextView(MainActivity.this);
                                vrstaTextView.setText(vrsta);
                                vrstaTextView.setPadding(16, 8, 16, 8); // Adjust padding as needed
                                vrstaTextView.setTextSize(18); // Set font size
                                vrstaTextView.setTextColor(Color.BLACK); // Set text color to black
                                vrstaTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1)); // Align to right

                                // Add TextViews to the row
                                row.addView(vsotaTextView);
                                row.addView(vrstaTextView);

                                // Add a separator line between rows
                                View separator = new View(MainActivity.this);
                                separator.setBackgroundColor(Color.GRAY);
                                separator.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)); // Set height to 1px
                                lastInputsTable.addView(separator);

                                lastInputsTable.addView(row);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: No data available", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private boolean isTokenStored() { //preverjanje ali je JWT shranjen v SharedPreferences
        SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
        return preferences.contains("jwt_token");
    }

    private String getTokenFromPreferences() { //preberi JWT iz shrambe
        SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
        return preferences.getString("jwt_token", null);
    }

    private void removeTokenFromPreferences() { //odstrani JWT iz shrambe
        SharedPreferences preferences = getSharedPreferences("Your_Preference_Name", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("jwt_token");
        editor.apply();
    }

    private void showAlert(String title, String message) { //za prikazovanje Alerta(prikaz žetona)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //meni
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        menu.add(0, MENU_POSODOBI_PODATKE, 0, getString(R.string.podatki_uporabnika));
        menu.add(0, MENU_VNOSI, 0, getString(R.string.vsi_vnosi));
        menu.add(0, MENU_JWT, 0, getString(R.string.napis_JWT));
        menu.add(0, MENU_IZHOD, 0, getString(R.string.napis_izhod));


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //dogodki za določen meni
        // switch stavek s testiranjem menijske postavke, ki je bila izbrana
        switch (item.getItemId()) {
            case MENU_POSODOBI_PODATKE:
                Intent intent = new Intent(this, UserUpdateActivity.class);
                startActivity(intent);
                return true;

            case MENU_VNOSI:
                Intent intent2 = new Intent(this, VnosiActivity.class);
                startActivity(intent2);
                return true;

            case MENU_JWT:
                String token = getTokenFromPreferences();
                if (token != null) {
                    // Display an alert with the JWT
                    showAlert("JWT Token", token);
                } else {
                    Toast.makeText(MainActivity.this, "JWT Token not found", Toast.LENGTH_SHORT).show();
                }
                return true;

            case MENU_IZHOD:
                removeTokenFromPreferences();
                finish(); // zaključi aktivnost
                return true;
        }
        return true;
    }
}
